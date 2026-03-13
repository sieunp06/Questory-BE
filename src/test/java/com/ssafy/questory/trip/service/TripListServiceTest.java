package com.ssafy.questory.trip.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.trip.dto.response.TripDetailResponseDto;
import com.ssafy.questory.trip.dto.response.TripSummaryResponseDto;
import com.ssafy.questory.trip.dto.row.TripDetailRow;
import com.ssafy.questory.trip.repository.TripListRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TripListServiceTest {

    @Mock
    private TripListRepository tripListRepository;

    @Mock
    private SecurityMember securityMember;

    @InjectMocks
    private TripListService tripListService;

    @Nested
    @DisplayName("파티 기준 여행 목록 조회")
    class GetTripsByPartyTest {

        @Test
        @DisplayName("파티 멤버면 여행 목록을 조회할 수 있다")
        void getTripsByParty_success() {
            // given
            Long memberId = 1L;
            Long partyId = 10L;

            List<TripSummaryResponseDto> trips = List.of(
                    new TripSummaryResponseDto(
                            100L,
                            "부산 여행",
                            "맛집 투어",
                            LocalDate.of(2026, 3, 20),
                            LocalDate.of(2026, 3, 22),
                            1L
                    ),
                    new TripSummaryResponseDto(
                            101L,
                            "제주 여행",
                            "힐링 여행",
                            LocalDate.of(2026, 4, 1),
                            LocalDate.of(2026, 4, 3),
                            2L
                    )
            );

            given(securityMember.getMemberId()).willReturn(memberId);
            given(tripListRepository.existsPartyMember(partyId, memberId)).willReturn(true);
            given(tripListRepository.findTripsByPartyId(partyId)).willReturn(trips);

            // when
            List<TripSummaryResponseDto> result = tripListService.getTripsByParty(securityMember, partyId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).tripId()).isEqualTo(100L);
            assertThat(result.get(0).title()).isEqualTo("부산 여행");
            assertThat(result.get(0).description()).isEqualTo("맛집 투어");
            assertThat(result.get(0).startDate()).isEqualTo(LocalDate.of(2026, 3, 20));
            assertThat(result.get(0).endDate()).isEqualTo(LocalDate.of(2026, 3, 22));
            assertThat(result.get(0).creatorId()).isEqualTo(1L);

            assertThat(result.get(1).tripId()).isEqualTo(101L);
            assertThat(result.get(1).title()).isEqualTo("제주 여행");
            assertThat(result.get(1).description()).isEqualTo("힐링 여행");
            assertThat(result.get(1).startDate()).isEqualTo(LocalDate.of(2026, 4, 1));
            assertThat(result.get(1).endDate()).isEqualTo(LocalDate.of(2026, 4, 3));
            assertThat(result.get(1).creatorId()).isEqualTo(2L);

            then(tripListRepository).should().existsPartyMember(partyId, memberId);
            then(tripListRepository).should().findTripsByPartyId(partyId);
        }

        @Test
        @DisplayName("파티 멤버가 아니면 예외가 발생한다")
        void getTripsByParty_fail_notPartyMember() {
            // given
            Long memberId = 1L;
            Long partyId = 10L;

            given(securityMember.getMemberId()).willReturn(memberId);
            given(tripListRepository.existsPartyMember(partyId, memberId)).willReturn(false);

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripListService.getTripsByParty(securityMember, partyId),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PARTY_MEMBER_NOT_FOUND);

            then(tripListRepository).should().existsPartyMember(partyId, memberId);
            then(tripListRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("여행 상세 조회")
    class GetTripDetailTest {

        @Test
        @DisplayName("여행 상세와 day, schedule을 조합해서 반환한다")
        void getTripDetail_success() throws Exception {
            // given
            Long memberId = 1L;
            Long partyId = 10L;
            Long tripId = 100L;

            given(securityMember.getMemberId()).willReturn(memberId);
            given(tripListRepository.existsPartyMember(partyId, memberId)).willReturn(true);
            given(tripListRepository.findTripDetailRows(partyId, tripId)).willReturn(List.of(
                    row(
                            tripId, partyId, 1L, "부산 여행", "맛집 투어",
                            LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 22),
                            1000L, 1, LocalDate.of(2026, 3, 20),
                            5000L, 111, "광안리", "야경 보기", 1, 1L
                    ),
                    row(
                            tripId, partyId, 1L, "부산 여행", "맛집 투어",
                            LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 22),
                            1000L, 1, LocalDate.of(2026, 3, 20),
                            5001L, 222, "해운대", "산책", 2, 1L
                    ),
                    row(
                            tripId, partyId, 1L, "부산 여행", "맛집 투어",
                            LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 22),
                            1001L, 2, LocalDate.of(2026, 3, 21),
                            5002L, 333, "돼지국밥", "점심", 1, 2L
                    )
            ));

            // when
            TripDetailResponseDto result = tripListService.getTripDetail(securityMember, partyId, tripId);

            // then
            assertThat(result.tripId()).isEqualTo(tripId);
            assertThat(result.partyId()).isEqualTo(partyId);
            assertThat(result.creatorId()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("부산 여행");
            assertThat(result.description()).isEqualTo("맛집 투어");
            assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 3, 20));
            assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 22));

            assertThat(result.days()).hasSize(2);

            assertThat(result.days().get(0).tripDayId()).isEqualTo(1000L);
            assertThat(result.days().get(0).dayNum()).isEqualTo(1);
            assertThat(result.days().get(0).tripDate()).isEqualTo(LocalDate.of(2026, 3, 20));
            assertThat(result.days().get(0).schedules()).hasSize(2);

            assertThat(result.days().get(0).schedules().get(0).tripScheduleId()).isEqualTo(5000L);
            assertThat(result.days().get(0).schedules().get(0).attractionNo()).isEqualTo(111);
            assertThat(result.days().get(0).schedules().get(0).title()).isEqualTo("광안리");
            assertThat(result.days().get(0).schedules().get(0).memo()).isEqualTo("야경 보기");
            assertThat(result.days().get(0).schedules().get(0).sortOrder()).isEqualTo(1);
            assertThat(result.days().get(0).schedules().get(0).createdBy()).isEqualTo(1L);

            assertThat(result.days().get(0).schedules().get(1).tripScheduleId()).isEqualTo(5001L);
            assertThat(result.days().get(0).schedules().get(1).attractionNo()).isEqualTo(222);
            assertThat(result.days().get(0).schedules().get(1).title()).isEqualTo("해운대");
            assertThat(result.days().get(0).schedules().get(1).memo()).isEqualTo("산책");
            assertThat(result.days().get(0).schedules().get(1).sortOrder()).isEqualTo(2);
            assertThat(result.days().get(0).schedules().get(1).createdBy()).isEqualTo(1L);

            assertThat(result.days().get(1).tripDayId()).isEqualTo(1001L);
            assertThat(result.days().get(1).dayNum()).isEqualTo(2);
            assertThat(result.days().get(1).tripDate()).isEqualTo(LocalDate.of(2026, 3, 21));
            assertThat(result.days().get(1).schedules()).hasSize(1);

            assertThat(result.days().get(1).schedules().get(0).tripScheduleId()).isEqualTo(5002L);
            assertThat(result.days().get(1).schedules().get(0).attractionNo()).isEqualTo(333);
            assertThat(result.days().get(1).schedules().get(0).title()).isEqualTo("돼지국밥");
            assertThat(result.days().get(1).schedules().get(0).memo()).isEqualTo("점심");
            assertThat(result.days().get(1).schedules().get(0).sortOrder()).isEqualTo(1);
            assertThat(result.days().get(1).schedules().get(0).createdBy()).isEqualTo(2L);

            then(tripListRepository).should().existsPartyMember(partyId, memberId);
            then(tripListRepository).should().findTripDetailRows(partyId, tripId);
        }

        @Test
        @DisplayName("일정이 없는 day도 포함해서 반환한다")
        void getTripDetail_success_withoutSchedule() throws Exception {
            // given
            Long memberId = 1L;
            Long partyId = 10L;
            Long tripId = 100L;

            given(securityMember.getMemberId()).willReturn(memberId);
            given(tripListRepository.existsPartyMember(partyId, memberId)).willReturn(true);
            given(tripListRepository.findTripDetailRows(partyId, tripId)).willReturn(List.of(
                    row(
                            tripId, partyId, 1L, "부산 여행", "맛집 투어",
                            LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 22),
                            1000L, 1, LocalDate.of(2026, 3, 20),
                            null, null, null, null, null, null
                    ),
                    row(
                            tripId, partyId, 1L, "부산 여행", "맛집 투어",
                            LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 22),
                            1001L, 2, LocalDate.of(2026, 3, 21),
                            5002L, 333, "돼지국밥", "점심", 1, 2L
                    )
            ));

            // when
            TripDetailResponseDto result = tripListService.getTripDetail(securityMember, partyId, tripId);

            // then
            assertThat(result.days()).hasSize(2);

            assertThat(result.days().get(0).tripDayId()).isEqualTo(1000L);
            assertThat(result.days().get(0).dayNum()).isEqualTo(1);
            assertThat(result.days().get(0).tripDate()).isEqualTo(LocalDate.of(2026, 3, 20));
            assertThat(result.days().get(0).schedules()).isEmpty();

            assertThat(result.days().get(1).tripDayId()).isEqualTo(1001L);
            assertThat(result.days().get(1).dayNum()).isEqualTo(2);
            assertThat(result.days().get(1).tripDate()).isEqualTo(LocalDate.of(2026, 3, 21));
            assertThat(result.days().get(1).schedules()).hasSize(1);
            assertThat(result.days().get(1).schedules().get(0).tripScheduleId()).isEqualTo(5002L);
        }

        @Test
        @DisplayName("trip_day가 null인 row는 무시하고 days는 비어 있게 반환한다")
        void getTripDetail_success_withoutTripDay() throws Exception {
            // given
            Long memberId = 1L;
            Long partyId = 10L;
            Long tripId = 100L;

            given(securityMember.getMemberId()).willReturn(memberId);
            given(tripListRepository.existsPartyMember(partyId, memberId)).willReturn(true);
            given(tripListRepository.findTripDetailRows(partyId, tripId)).willReturn(List.of(
                    row(
                            tripId, partyId, 1L, "부산 여행", "맛집 투어",
                            LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 22),
                            null, null, null,
                            null, null, null, null, null, null
                    )
            ));

            // when
            TripDetailResponseDto result = tripListService.getTripDetail(securityMember, partyId, tripId);

            // then
            assertThat(result.tripId()).isEqualTo(tripId);
            assertThat(result.days()).isEmpty();
        }

        @Test
        @DisplayName("row가 비어 있으면 여행이 없는 것으로 처리한다")
        void getTripDetail_fail_tripNotFound() {
            // given
            Long memberId = 1L;
            Long partyId = 10L;
            Long tripId = 100L;

            given(securityMember.getMemberId()).willReturn(memberId);
            given(tripListRepository.existsPartyMember(partyId, memberId)).willReturn(true);
            given(tripListRepository.findTripDetailRows(partyId, tripId)).willReturn(List.of());

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripListService.getTripDetail(securityMember, partyId, tripId),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TRIP_NOT_FOUND);

            then(tripListRepository).should().existsPartyMember(partyId, memberId);
            then(tripListRepository).should().findTripDetailRows(partyId, tripId);
        }

        @Test
        @DisplayName("파티 멤버가 아니면 상세 조회할 수 없다")
        void getTripDetail_fail_notPartyMember() {
            // given
            Long memberId = 1L;
            Long partyId = 10L;
            Long tripId = 100L;

            given(securityMember.getMemberId()).willReturn(memberId);
            given(tripListRepository.existsPartyMember(partyId, memberId)).willReturn(false);

            // when
            CustomException exception = catchThrowableOfType(
                    () -> tripListService.getTripDetail(securityMember, partyId, tripId),
                    CustomException.class
            );

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PARTY_MEMBER_NOT_FOUND);

            then(tripListRepository).should().existsPartyMember(partyId, memberId);
            then(tripListRepository).shouldHaveNoMoreInteractions();
        }
    }

    private TripDetailRow row(
            Long tripId,
            Long partyId,
            Long creatorId,
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Long tripDayId,
            Integer dayNum,
            LocalDate tripDate,
            Long tripScheduleId,
            Integer attractionNo,
            String scheduleTitle,
            String memo,
            Integer sortOrder,
            Long createdBy
    ) throws Exception {
        TripDetailRow row = new TripDetailRow();

        setField(row, "tripId", tripId);
        setField(row, "partyId", partyId);
        setField(row, "creatorId", creatorId);
        setField(row, "title", title);
        setField(row, "description", description);
        setField(row, "startDate", startDate);
        setField(row, "endDate", endDate);

        setField(row, "tripDayId", tripDayId);
        setField(row, "dayNum", dayNum);
        setField(row, "tripDate", tripDate);

        setField(row, "tripScheduleId", tripScheduleId);
        setField(row, "attractionNo", attractionNo);
        setField(row, "scheduleTitle", scheduleTitle);
        setField(row, "memo", memo);
        setField(row, "sortOrder", sortOrder);
        setField(row, "createdBy", createdBy);

        return row;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}