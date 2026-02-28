package com.ssafy.questory.friend;

import com.ssafy.questory.friend.service.FriendService;
import com.ssafy.questory.mail.dto.request.MemberEmailRequestDto;
import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
public class FriendConcurrencyTest {
    @Autowired FriendService friendService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    private Long senderId;
    private Long receiverId;
    private String senderEmail = "sender_conc@test.com";
    private String receiverEmail = "receiver_conc@test.com";

    @BeforeEach
    void setUp() {
        senderId = ensureMember(senderEmail, "sender");
        receiverId = ensureMember(receiverEmail, "receiver");
        cleanup(senderId, receiverId);
    }

    @AfterEach
    void tearDown() {
        cleanup(senderId, receiverId);
    }

    @Test
    void request_race_condition_test() throws Exception {

        Member senderMember = memberRepository.findById(senderId).orElseThrow();
        SecurityMember sender = SecurityMember.fromMember(senderMember);

        MemberEmailRequestDto dto = new MemberEmailRequestDto(receiverEmail);

        int threadCount = 30;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    friendService.request(sender, dto);
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        Integer pendingCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM friend_request
            WHERE sender_id = ?
              AND receiver_id = ?
              AND status = 'PENDING'
        """, Integer.class, senderId, receiverId);

        System.out.println("PENDING COUNT = " + pendingCount);

        assertThat(pendingCount).isLessThanOrEqualTo(1);
    }


    @Test
    void accept_race_condition_test() throws Exception {

        Long requestId = insertPendingRequest(senderId, receiverId);

        Member receiverMember = memberRepository.findById(receiverId).orElseThrow();
        SecurityMember receiver = SecurityMember.fromMember(receiverMember);

        ExecutorService pool = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    friendService.acceptRequest(receiver, requestId);
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        Integer friendExists = jdbcTemplate.queryForObject("""
            SELECT EXISTS(
                SELECT 1 FROM friend
                WHERE member_a_id = LEAST(?,?)
                  AND member_b_id = GREATEST(?,?)
            )
        """, Integer.class, senderId, receiverId, senderId, receiverId);

        String status = jdbcTemplate.queryForObject("""
            SELECT status FROM friend_request
            WHERE friend_request_id = ?
        """, String.class, requestId);

        System.out.println("friendExists=" + friendExists + ", status=" + status);

        assertThat(friendExists).isEqualTo(1);
        assertThat(status).isEqualTo("ACCEPTED");
    }


    @Test
    void accept_vs_reject_race_test() throws Exception {

        Long requestId = insertPendingRequest(senderId, receiverId);

        Member receiverMember = memberRepository.findById(receiverId).orElseThrow();
        SecurityMember receiver = SecurityMember.fromMember(receiverMember);

        ExecutorService pool = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        pool.submit(() -> {
            ready.countDown();
            try {
                start.await();
                friendService.acceptRequest(receiver, requestId);
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        });

        pool.submit(() -> {
            ready.countDown();
            try {
                start.await();
                friendService.rejectRequest(receiver, requestId);
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        });

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        Integer friendExists = jdbcTemplate.queryForObject("""
            SELECT EXISTS(
                SELECT 1 FROM friend
                WHERE member_a_id = LEAST(?,?)
                  AND member_b_id = GREATEST(?,?)
            )
        """, Integer.class, senderId, receiverId, senderId, receiverId);

        String status = jdbcTemplate.queryForObject("""
            SELECT status FROM friend_request
            WHERE friend_request_id = ?
        """, String.class, requestId);

        System.out.println("friendExists=" + friendExists + ", status=" + status);

        // 불일치 감지
        if (friendExists == 1 && !"ACCEPTED".equals(status)) {
            fail("친구는 생성됐는데 요청 상태가 ACCEPTED가 아님 → 레이스 발생");
        }
    }

    private Long ensureMember(String email, String nickname) {
        return memberRepository.findByEmail(email)
                .map(Member::getMemberId)
                .orElseGet(() -> {
                    jdbcTemplate.update("""
                        INSERT INTO member(email, nickname, total_exp, status)
                        VALUES (?, ?, 0, 'NORMAL')
                    """, email, nickname);
                    return memberRepository.findByEmail(email).orElseThrow().getMemberId();
                });
    }

    private void cleanup(Long a, Long b) {
        jdbcTemplate.update("""
            DELETE FROM friend_request
            WHERE (sender_id=? AND receiver_id=?)
               OR (sender_id=? AND receiver_id=?)
        """, a, b, b, a);

        jdbcTemplate.update("""
            DELETE FROM friend
            WHERE member_a_id = LEAST(?,?)
              AND member_b_id = GREATEST(?,?)
        """, a, b, a, b);
    }

    private Long insertPendingRequest(Long sender, Long receiver) {
        jdbcTemplate.update("""
            INSERT INTO friend_request(sender_id, receiver_id, status, created_at)
            VALUES (?, ?, 'PENDING', NOW())
        """, sender, receiver);

        return jdbcTemplate.queryForObject("""
            SELECT friend_request_id
            FROM friend_request
            WHERE sender_id=? AND receiver_id=?
            ORDER BY friend_request_id DESC
            LIMIT 1
        """, Long.class, sender, receiver);
    }
}