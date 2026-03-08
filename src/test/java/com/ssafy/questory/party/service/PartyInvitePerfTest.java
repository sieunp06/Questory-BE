package com.ssafy.questory.party.service;

import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.repository.MemberRepository;
import com.ssafy.questory.party.dto.request.InviteRequestDto;
import com.ssafy.questory.support.QueryCountConfig;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "decorator.datasource.p6spy.enable-logging=false"
        }
)
@Import(QueryCountConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartyInvitePerfTest {

    private static final long PARTY_ID = 1L;
    private static final long INVITER_ID = 1L;
    private static final int MAX_MEMBER_ID = 101;
    private static final int CHUNK_SIZE = 15;

    @Autowired PartyInviteService partyInviteService;
    @Autowired JdbcTemplate jdbc;
    @Autowired MemberRepository memberRepository;

    private final List<ResultRow> rows = new ArrayList<>();

    @BeforeAll
    void globalSetup() {
        ensureMembers(MAX_MEMBER_ID);
        ensureParty(PARTY_ID, INVITER_ID);
        ensureOwner(PARTY_ID, INVITER_ID);
        ensureFriends(INVITER_ID, MAX_MEMBER_ID - 1); // 2 ~ 101
    }

    @BeforeEach
    void beforeEach() {
        cleanupInvites(PARTY_ID, INVITER_ID);
        QueryCountHolder.clear();
    }

    @AfterEach
    void afterEach() {
        cleanupInvites(PARTY_ID, INVITER_ID);
    }

    @AfterAll
    void printSummary() {
        System.out.println();
        System.out.println("=== Party Invite Performance Summary ===");
        System.out.println("| N | Select | Insert | Update | Delete | Total | Time(ms) |");
        System.out.println("|---:|---:|---:|---:|---:|---:|---:|");
        for (ResultRow r : rows) {
            System.out.printf("| %d | %d | %d | %d | %d | %d | %d |%n",
                    r.n, r.select, r.insert, r.update, r.delete, r.total, r.timeMs);
        }
        System.out.println();
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 50, 100})
    void invite_perf(int n) {
        List<Long> inviteeIds = LongStream.rangeClosed(2, n + 1L).boxed().toList();
        InviteRequestDto dto = new InviteRequestDto(inviteeIds);

        SecurityMember principal = inviterPrincipalFromDb();

        QueryCountHolder.clear();
        long t0 = System.nanoTime();

        var response = partyInviteService.invite(principal, PARTY_ID, dto);

        long t1 = System.nanoTime();
        long elapsedMs = (t1 - t0) / 1_000_000;

        QueryCount qc = QueryCountHolder.getGrandTotal();
        long select = qc.getSelect();
        long insert = qc.getInsert();
        long update = qc.getUpdate();
        long delete = qc.getDelete();
        long total = select + insert + update + delete;

        assertThat(response.results()).hasSize(n);

        long expectedMaxInsert = (n + CHUNK_SIZE - 1) / CHUNK_SIZE;
        assertThat(insert).isLessThanOrEqualTo(expectedMaxInsert);

        rows.add(new ResultRow(n, select, insert, update, delete, total, elapsedMs));

        System.out.printf("[N=%d] select=%d insert=%d update=%d delete=%d total=%d time=%dms%n",
                n, select, insert, update, delete, total, elapsedMs);
    }

    private SecurityMember inviterPrincipalFromDb() {
        Member inviter = memberRepository.findById(INVITER_ID)
                .orElseThrow(() -> new IllegalStateException("inviter not found: " + INVITER_ID));

        return SecurityMember.fromMember(inviter);
    }

    private void ensureMembers(int maxIdInclusive) {
        for (long id = 1; id <= maxIdInclusive; id++) {
            String email = "user" + id + "@dummy.questory.local";
            String nickname = "user" + id;

            jdbc.update("""
                INSERT INTO member (member_id, email, nickname, status)
                VALUES (?, ?, ?, 'NORMAL')
                ON DUPLICATE KEY UPDATE
                    email = VALUES(email),
                    nickname = VALUES(nickname),
                    status = VALUES(status)
                """, id, email, nickname);
        }
    }

    private void ensureParty(long partyId, long creatorId) {
        jdbc.update("""
            INSERT INTO party (party_id, name, creator_id)
            VALUES (?, 'TEST_PARTY_1', ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                creator_id = VALUES(creator_id)
            """, partyId, creatorId);
    }

    private void ensureOwner(long partyId, long ownerId) {
        jdbc.update("""
            INSERT INTO party_member (party_id, member_id, role)
            VALUES (?, ?, 'OWNER')
            ON DUPLICATE KEY UPDATE
                role = 'OWNER'
            """, partyId, ownerId);
    }

    private void ensureFriends(long inviterId, int count) {
        List<Object[]> args = LongStream.rangeClosed(2, count + 1L)
                .mapToObj(invitee -> new Object[]{inviterId, invitee})
                .toList();

        jdbc.batchUpdate("""
            INSERT INTO friend (member_a_id, member_b_id)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE
                member_a_id = member_a_id
            """, args);
    }

    private void cleanupInvites(long partyId, long inviterId) {
        jdbc.update("""
            DELETE FROM party_invite
            WHERE party_id = ? AND inviter_id = ?
            """, partyId, inviterId);
    }

    private record ResultRow(
            int n,
            long select,
            long insert,
            long update,
            long delete,
            long total,
            long timeMs
    ) {}
}