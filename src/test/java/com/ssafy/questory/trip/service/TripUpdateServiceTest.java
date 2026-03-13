package com.ssafy.questory.trip.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.trip.domain.Trip;
import com.ssafy.questory.trip.domain.TripDay;
import com.ssafy.questory.trip.domain.TripScheduleInsertCommand;
import com.ssafy.questory.trip.domain.TripScheduleSnapshot;
import com.ssafy.questory.trip.dto.request.UpdateRequestDto;
import com.ssafy.questory.trip.repository.TripDayRepository;
import com.ssafy.questory.trip.repository.TripRepository;
import com.ssafy.questory.trip.repository.TripScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TripUpdateServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripDayRepository tripDayRepository;

    @Mock
    private TripScheduleRepository tripScheduleRepository;

    @Mock
    private SecurityMember securityMember;

    @InjectMocks
    private TripUpdateService tripUpdateService;

    @Nested
    @DisplayName("여행 정보 수정")
    class UpdateInfoTest {

        @Test
        @DisplayName("여행 정보와 날짜를 수정하고 일정도 새 날짜 기준으로 재배치한다")
        void updateInfo_success_withScheduleReallocation() {
            // given
            Long memberId = 1L;
            Long tripId = 100L;
            Long partyId = 10L;

            Trip trip = Trip.builder()
                    .tripId(tripId)
                    .partyId(partyId)
                    .creatorId(memberId)
                    .title("기존 여행")
                    .description("기존 설명")
                    .startDate(LocalDate.of(2026, 3, 1))
                    .endDate(LocalDate.of(2026, 3, 5))
                    .build();

            UpdateRequestDto dto = new UpdateRequestDto(
                    "수정된 여행",
                    "수정된 설명",
                    LocalDate.of(2026, 3, 2),
                    LocalDate.of(2026, 3, 4)
            );

            List<TripScheduleSnapshot> snapshots = List.of(
                    snapshot(11L, LocalDate.of(2026, 3, 1), 2, 1001, "A", "memoA", 1L),
                    snapshot(12L, LocalDate.of(2026, 3, 1), 1, 1002, "B", "memoB", 2L),
                    snapshot(13L, LocalDate.of(2026, 3, 3), 1, 1003, "C", "memoC", 1L),
                    snapshot(14L, LocalDate.of(2026, 3, 5), 1, 1004, "D", "memoD", 3L)
            );

            List<TripDay> recreatedTripDays = List.of(
                    tripDay(1000L, LocalDate.of(2026, 3, 2)),
                    tripDay(1001L, LocalDate.of(2026, 3, 3)),
                    tripDay(1002L, LocalDate.of(2026, 3, 4))
            );

            given(securityMember.getMemberId()).willReturn(memberId);
            given(tripRepository.findById(tripId)).willReturn(trip);
            given(tripScheduleRepository.findSnapshotsByTripId(tripId)).willReturn(snapshots);
            given(tripDayRepository.findByTripId(tripId)).willReturn(recreatedTripDays);

            // when
            tripUpdateService.updateInfo(securityMember, tripId, dto);

            // then
            ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
            then(tripRepository).should().updateInfo(tripCaptor.capture());

            Trip updatedTrip = tripCaptor.getValue();
            assertThat(updatedTrip.getTripId()).isEqualTo(tripId);
            assertThat(updatedTrip.getPartyId()).isEqualTo(partyId);
            assertThat(updatedTrip.getCreatorId()).isEqualTo(memberId);
            assertThat(updatedTrip.getTitle()).isEqualTo("수정된 여행");
            assertThat(updatedTrip.getDescription()).isEqualTo("수정된 설명");
            assertThat(updatedTrip.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 2));
            assertThat(updatedTrip.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 4));

            then(tripDayRepository).should().deleteByTripId(tripId);

            ArgumentCaptor<List<LocalDate>> datesCaptor = ArgumentCaptor.forClass(List.class);
            then(tripDayRepository).should().bulkInsert(eq(tripId), datesCaptor.capture());
            assertThat(datesCaptor.getValue()).containsExactly(
                    LocalDate.of(2026, 3, 2),
                    LocalDate.of(2026, 3, 3),
                    LocalDate.of(2026, 3, 4)
            );

            ArgumentCaptor<List<TripScheduleInsertCommand>> commandsCaptor = ArgumentCaptor.forClass(List.class);
            then(tripScheduleRepository).should().bulkInsert(commandsCaptor.capture());

            List<TripScheduleInsertCommand> commands = commandsCaptor.getValue();
            assertThat(commands).hasSize(4);

            assertThat(commands.get(0).getTripDayId()).isEqualTo(1000L);
            assertThat(commands.get(0).getAttractionNo()).isEqualTo(1002);
            assertThat(commands.get(0).getTitle()).isEqualTo("B");
            assertThat(commands.get(0).getMemo()).isEqualTo("memoB");
            assertThat(commands.get(0).getSortOrder()).isEqualTo(1);
            assertThat(commands.get(0).getCreatedBy()).isEqualTo(2L);

            assertThat(commands.get(1).getTripDayId()).isEqualTo(1000L);
            assertThat(commands.get(1).getAttractionNo()).isEqualTo(1001);
            assertThat(commands.get(1).getTitle()).isEqualTo("A");
            assertThat(commands.get(1).getMemo()).isEqualTo("memoA");
            assertThat(commands.get(1).getSortOrder()).isEqualTo(2);
            assertThat(commands.get(1).getCreatedBy()).isEqualTo(1L);

            assertThat(commands.get(2).getTripDayId()).isEqualTo(1001L);
            assertThat(commands.get(2).getAttractionNo()).isEqualTo(1003);
            assertThat(commands.get(2).getTitle()).isEqualTo("C");
            assertThat(commands.get(2).getMemo()).isEqualTo("memoC");
            assertThat(commands.get(2).getSortOrder()).isEqualTo(1);
            assertThat(commands.get(2).getCreatedBy()).isEqualTo(1L);

            assertThat(commands.get(3).getTripDayId()).isEqualTo(1002L);
            assertThat(commands.get(3).getAttractionNo()).isEqualTo(1004);
            assertThat(commands.get(3).getTitle()).isEqualTo("D");
            assertThat(commands.get(3).getMemo()).isEqualTo("memoD");
            assertThat(commands.get(3).getSortOrder()).isEqualTo(1);
            assertThat(commands.get(3).getCreatedBy()).isEqualTo(3L);
        }

        @Test
        @DisplayName("스냅샷이 없으면 일정 bulk insert는 호출되지 않는다")
        void updateInfo_success_withoutSchedules() {
            // given
            Long memberId = 1L;
            Long tripId = 100L;
            Long partyId = 10L;

            Trip trip = Trip.builder()
                    .tripId(tripId)
                    .partyId(partyId)
                    .creatorId(memberId)
                    .title("기존 여행")
                    .description("기존 설명")
                    .startDate(LocalDate.of(2026, 3, 1))
                    .endDate(LocalDate.of(2026, 3, 5))
                    .build();

            UpdateRequestDto dto = new UpdateRequestDto(
                    "수정된 여행",
                    "수정된 설명",
                    LocalDate.of(2026, 3, 2),
                    LocalDate.of(2026, 3, 4)
            );

            List<TripDay> recreatedTripDays = List.of(
                    tripDay(1000L, LocalDate.of(2026, 3, 2)),
                    tripDay(1001L, LocalDate.of(2026, 3, 3)),
                    tripDay(1002L, LocalDate.of(2026, 3, 4))
            );

            given(securityMember.getMemberId()).willReturn(memberId);
            given(tripRepository.findById(tripId)).willReturn(trip);
            given(tripScheduleRepository.findSnapshotsByTripId(tripId)).willReturn(List.of());
            given(tripDayRepository.findByTripId(tripId)).willReturn(recreatedTripDays);

            // when
            tripUpdateService.updateInfo(securityMember, tripId, dto);

            // then
            then(tripRepository).should().updateInfo(any(Trip.class));
            then(tripDayRepository).should().deleteByTripId(tripId);
            then(tripDayRepository).should().bulkInsert(eq(tripId), any(List.class));
            then(tripScheduleRepository).should(never()).bulkInsert(any(List.class));
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
        void updateInfo_fail_invalidDateRange() {
            // given
            UpdateRequestDto dto = new UpdateRequestDto(
                    "수정된 여행",
                    "수정된 설명",
                    LocalDate.of(2026, 3, 5),
                    LocalDate.of(2026, 3, 4)
            );

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripUpdateService.updateInfo(securityMember, 100L, dto),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);

            then(tripRepository).shouldHaveNoInteractions();
            then(tripDayRepository).shouldHaveNoInteractions();
            then(tripScheduleRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("여행이 없으면 예외가 발생한다")
        void updateInfo_fail_tripNotFound() {
            // given
            Long tripId = 100L;

            UpdateRequestDto dto = new UpdateRequestDto(
                    "수정된 여행",
                    "수정된 설명",
                    LocalDate.of(2026, 3, 2),
                    LocalDate.of(2026, 3, 4)
            );

            given(tripRepository.findById(tripId)).willReturn(null);

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripUpdateService.updateInfo(securityMember, tripId, dto),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TRIP_NOT_FOUND);

            then(tripRepository).should().findById(tripId);
            then(tripRepository).should(never()).updateInfo(any(Trip.class));
            then(tripDayRepository).shouldHaveNoInteractions();
            then(tripScheduleRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("생성자가 아니면 수정할 수 없다")
        void updateInfo_fail_forbidden() {
            // given
            Long tripId = 100L;

            Trip trip = Trip.builder()
                    .tripId(tripId)
                    .partyId(10L)
                    .creatorId(999L)
                    .title("기존 여행")
                    .description("기존 설명")
                    .startDate(LocalDate.of(2026, 3, 1))
                    .endDate(LocalDate.of(2026, 3, 5))
                    .build();

            UpdateRequestDto dto = new UpdateRequestDto(
                    "수정된 여행",
                    "수정된 설명",
                    LocalDate.of(2026, 3, 2),
                    LocalDate.of(2026, 3, 4)
            );

            given(securityMember.getMemberId()).willReturn(1L);
            given(tripRepository.findById(tripId)).willReturn(trip);

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripUpdateService.updateInfo(securityMember, tripId, dto),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_PARTY_CREATOR_ONLY);

            then(tripRepository).should().findById(tripId);
            then(tripRepository).should(never()).updateInfo(any(Trip.class));
            then(tripDayRepository).shouldHaveNoInteractions();
            then(tripScheduleRepository).shouldHaveNoInteractions();
        }
    }

    private TripDay tripDay(Long tripDayId, LocalDate tripDate) {
        TripDay tripDay = mock(TripDay.class);
        given(tripDay.getTripDayId()).willReturn(tripDayId);
        given(tripDay.getTripDate()).willReturn(tripDate);
        return tripDay;
    }

    private TripScheduleSnapshot snapshot(
            Long tripScheduleId,
            LocalDate oldTripDate,
            Integer sortOrder,
            Integer attractionNo,
            String title,
            String memo,
            Long createdBy
    ) {
        return TripScheduleSnapshot.builder()
                .tripScheduleId(tripScheduleId)
                .oldTripDate(oldTripDate)
                .attractionNo(attractionNo)
                .title(title)
                .memo(memo)
                .sortOrder(sortOrder)
                .createdBy(createdBy)
                .build();
    }
}