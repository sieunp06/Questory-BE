package com.ssafy.questory.trip.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.party.repository.PartyMemberRepository;
import com.ssafy.questory.trip.domain.Trip;
import com.ssafy.questory.trip.dto.request.CreateRequestDto;
import com.ssafy.questory.trip.dto.response.CreateResponseDto;
import com.ssafy.questory.trip.repository.TripDayRepository;
import com.ssafy.questory.trip.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripDayRepository tripDayRepository;

    @Mock
    private PartyMemberRepository partyMemberRepository;

    @Mock
    private SecurityMember securityMember;

    @InjectMocks
    private TripService tripService;

    @Nested
    @DisplayName("여행 생성")
    class CreateTest {

        @Test
        @DisplayName("정상적으로 여행을 생성하고 trip_day를 일괄 저장한다")
        void create_success() throws Exception {
            // given
            Long memberId = 1L;
            Long partyId = 10L;
            Long tripId = 100L;

            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = startDate.plusDays(2);

            CreateRequestDto dto = new CreateRequestDto(
                    partyId,
                    "부산 여행",
                    "맛집 투어",
                    startDate,
                    endDate
            );

            given(securityMember.getMemberId()).willReturn(memberId);
            given(partyMemberRepository.exists(partyId, memberId)).willReturn(true);

            willAnswer(invocation -> {
                Trip trip = invocation.getArgument(0);
                setField(trip, "tripId", tripId);
                return null;
            }).given(tripRepository).insert(any(Trip.class));

            // when
            CreateResponseDto result = tripService.create(securityMember, dto);

            // then
            ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
            then(tripRepository).should().insert(tripCaptor.capture());

            Trip savedTrip = tripCaptor.getValue();
            assertThat(savedTrip.getPartyId()).isEqualTo(partyId);
            assertThat(savedTrip.getCreatorId()).isEqualTo(memberId);
            assertThat(savedTrip.getTitle()).isEqualTo("부산 여행");
            assertThat(savedTrip.getDescription()).isEqualTo("맛집 투어");
            assertThat(savedTrip.getStartDate()).isEqualTo(startDate);
            assertThat(savedTrip.getEndDate()).isEqualTo(endDate);
            assertThat(savedTrip.getTripId()).isEqualTo(tripId);

            ArgumentCaptor<List<LocalDate>> datesCaptor = ArgumentCaptor.forClass(List.class);
            then(tripDayRepository).should().bulkInsert(eq(tripId), datesCaptor.capture());

            assertThat(datesCaptor.getValue()).containsExactly(
                    startDate,
                    startDate.plusDays(1),
                    endDate
            );

            assertThat(result.tripId()).isEqualTo(tripId);
            assertThat(result.title()).isEqualTo("부산 여행");
            assertThat(result.description()).isEqualTo("맛집 투어");
            assertThat(result.startDate()).isEqualTo(startDate);
            assertThat(result.endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
        void create_fail_invalidDateRange() {
            // given
            CreateRequestDto dto = new CreateRequestDto(
                    10L,
                    "부산 여행",
                    "맛집 투어",
                    LocalDate.of(2026, 3, 22),
                    LocalDate.of(2026, 3, 20)
            );

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripService.create(securityMember, dto),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);

            then(partyMemberRepository).shouldHaveNoInteractions();
            then(tripRepository).shouldHaveNoInteractions();
            then(tripDayRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("파티 멤버가 아니면 예외가 발생한다")
        void create_fail_notPartyMember() {
            // given
            Long memberId = 1L;
            Long partyId = 10L;

            CreateRequestDto dto = new CreateRequestDto(
                    partyId,
                    "부산 여행",
                    "맛집 투어",
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            given(securityMember.getMemberId()).willReturn(memberId);
            given(partyMemberRepository.exists(partyId, memberId)).willReturn(false);

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripService.create(securityMember, dto),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PARTY_MEMBER_NOT_FOUND);

            then(tripRepository).shouldHaveNoInteractions();
            then(tripDayRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("여행 삭제")
    class DeleteTest {

        @Test
        @DisplayName("생성자는 여행을 삭제할 수 있다")
        void delete_success() {
            // given
            Long tripId = 100L;
            Long creatorId = 1L;

            Trip trip = Trip.builder()
                    .tripId(tripId)
                    .partyId(10L)
                    .creatorId(creatorId)
                    .title("부산 여행")
                    .description("맛집 투어")
                    .startDate(LocalDate.of(2026, 3, 20))
                    .endDate(LocalDate.of(2026, 3, 22))
                    .build();

            given(tripRepository.findById(tripId)).willReturn(trip);
            given(securityMember.getMemberId()).willReturn(creatorId);
            given(tripRepository.deleteById(tripId)).willReturn(1);

            // when
            tripService.delete(securityMember, tripId);

            // then
            then(tripRepository).should().findById(tripId);
            then(tripRepository).should().deleteById(tripId);
        }

        @Test
        @DisplayName("여행이 없으면 예외가 발생한다")
        void delete_fail_tripNotFound() {
            // given
            Long tripId = 100L;
            given(tripRepository.findById(tripId)).willReturn(null);

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripService.delete(securityMember, tripId),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TRIP_NOT_FOUND);

            then(tripRepository).should().findById(tripId);
            then(tripRepository).should(never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("생성자가 아니면 삭제할 수 없다")
        void delete_fail_forbidden() {
            // given
            Long tripId = 100L;

            Trip trip = Trip.builder()
                    .tripId(tripId)
                    .partyId(10L)
                    .creatorId(1L)
                    .title("부산 여행")
                    .description("맛집 투어")
                    .startDate(LocalDate.of(2026, 3, 20))
                    .endDate(LocalDate.of(2026, 3, 22))
                    .build();

            given(tripRepository.findById(tripId)).willReturn(trip);
            given(securityMember.getMemberId()).willReturn(2L);

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripService.delete(securityMember, tripId),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_PARTY_CREATOR_ONLY);

            then(tripRepository).should().findById(tripId);
            then(tripRepository).should(never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("삭제 시점에 이미 삭제되었으면 예외가 발생한다")
        void delete_fail_deletedByOtherTransaction() {
            // given
            Long tripId = 100L;
            Long creatorId = 1L;

            Trip trip = Trip.builder()
                    .tripId(tripId)
                    .partyId(10L)
                    .creatorId(creatorId)
                    .title("부산 여행")
                    .description("맛집 투어")
                    .startDate(LocalDate.of(2026, 3, 20))
                    .endDate(LocalDate.of(2026, 3, 22))
                    .build();

            given(tripRepository.findById(tripId)).willReturn(trip);
            given(securityMember.getMemberId()).willReturn(creatorId);
            given(tripRepository.deleteById(tripId)).willReturn(0);

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripService.delete(securityMember, tripId),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TRIP_NOT_FOUND);

            then(tripRepository).should().findById(tripId);
            then(tripRepository).should().deleteById(tripId);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}