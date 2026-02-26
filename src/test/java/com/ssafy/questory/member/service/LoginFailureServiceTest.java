package com.ssafy.questory.member.service;

import com.ssafy.questory.member.repository.MemberPasswordCredentialsRepository;
import com.ssafy.questory.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginFailureServiceTest {
    @Mock
    MemberRepository memberRepository;

    @Mock
    MemberPasswordCredentialsRepository memberPasswordRepository;

    @InjectMocks
    LoginFailureService loginFailureService;

    @Test
    @DisplayName("updateFailedCount: 실패 카운트 증가 + lastFailedLoginAt 갱신 후, 최신 failedCount를 조회해서 반환한다")
    void updateFailedCount_success() {
        String email = "user@example.com";
        LocalDateTime now = LocalDateTime.of(2026, 2, 26, 1, 0, 0);

        when(memberPasswordRepository.findFailedCountByEmail(email)).thenReturn(3);

        int result = loginFailureService.updateFailedCount(email, now);

        assertThat(result).isEqualTo(3);

        InOrder inOrder = inOrder(memberPasswordRepository);
        inOrder.verify(memberPasswordRepository).increaseFailedLoginCountAndUpdateLastFailedLoginAt(email, now);
        inOrder.verify(memberPasswordRepository).findFailedCountByEmail(email);
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(memberRepository);
    }

    @Test
    @DisplayName("lockAccount: member status를 LOCKED로 변경하고, lockedUntil을 now+10분으로 저장한다")
    void lockAccount_success() {
        String email = "user@example.com";

        LocalDateTime before = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> lockedUntilCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        loginFailureService.lockAccount(email);

        LocalDateTime after = LocalDateTime.now();

        verify(memberRepository).changeStatusLocked(email);
        verify(memberPasswordRepository).lock(eq(email), lockedUntilCaptor.capture());

        LocalDateTime lockedUntil = lockedUntilCaptor.getValue();

        LocalDateTime minExpected = before.plusMinutes(10);
        LocalDateTime maxExpected = after.plusMinutes(10);

        assertThat(lockedUntil).isAfterOrEqualTo(minExpected);
        assertThat(lockedUntil).isBeforeOrEqualTo(maxExpected);

        verifyNoMoreInteractions(memberRepository, memberPasswordRepository);
    }

    @Test
    @DisplayName("unlockAccount: member status를 NORMAL로 변경하고, 잠금 정보를 해제한다")
    void unlockAccount_success() {
        String email = "user@example.com";

        loginFailureService.unlockAccount(email);

        verify(memberRepository).changeStatusNormal(email);
        verify(memberPasswordRepository).unlock(email);
        verifyNoMoreInteractions(memberRepository, memberPasswordRepository);
    }
}