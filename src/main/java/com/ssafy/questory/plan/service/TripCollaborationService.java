package com.ssafy.questory.plan.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.plan.dto.command.TripEditCommand;
import com.ssafy.questory.plan.dto.event.TripChangedAfterCommitEvent;
import com.ssafy.questory.plan.dto.event.TripChangedEvent;
import com.ssafy.questory.plan.dto.payload.*;
import com.ssafy.questory.plan.dto.ws.TripScheduleWsDto;
import com.ssafy.questory.trip.domain.TripScheduleInsertCommand;
import com.ssafy.questory.trip.domain.TripScheduleSnapshot;
import com.ssafy.questory.trip.repository.TripPermissionRepository;
import com.ssafy.questory.trip.repository.TripRepository;
import com.ssafy.questory.trip.repository.TripScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TripCollaborationService {
    private static final int TEMP_OFFSET = 1_000_000;
    private static final int TEMP_ORDER = 1_000_000;

    private final TripRepository tripRepository;
    private final TripPermissionRepository tripPermissionRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void add(Long tripId, Principal principal, TripEditCommand<AddSchedulePayload> command) {
        Long memberId = extractMemberId(principal);
        validateParticipant(tripId, memberId);
        validateRevision(tripId, command.getBaseRevision());

        AddSchedulePayload payload = command.getPayload();
        validateTripDayBelongsToTrip(tripId, payload.getTripDayId());

        Integer nextSortOrder = tripScheduleRepository.findNextSortOrder(payload.getTripDayId());

        TripScheduleInsertCommand insertCommand = TripScheduleInsertCommand.builder()
                .tripDayId(payload.getTripDayId())
                .attractionNo(payload.getAttractionNo())
                .title(payload.getTitle())
                .memo(payload.getMemo())
                .sortOrder(nextSortOrder)
                .createdBy(memberId)
                .build();

        tripScheduleRepository.insert(insertCommand);

        if (insertCommand.getTripScheduleId() == null) {
            throw new CustomException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
        }

        TripScheduleWsDto created = tripScheduleRepository.findWsDtoById(insertCommand.getTripScheduleId());

        tripRepository.increaseRevision(tripId);
        Long newRevision = tripRepository.findRevisionById(tripId);

        publishAfterCommit(tripId, TripChangedEvent.builder()
                .eventType("SCHEDULE_ADDED")
                .tripId(tripId)
                .revision(newRevision)
                .actorMemberId(memberId)
                .clientRequestId(command.getClientRequestId())
                .occurredAt(LocalDateTime.now())
                .payload(created)
                .build());
    }

    @Transactional
    public void updateMemo(Long tripId, Principal principal, TripEditCommand<UpdateMemoPayload> command) {
        Long memberId = extractMemberId(principal);
        validateParticipant(tripId, memberId);
        validateRevision(tripId, command.getBaseRevision());

        UpdateMemoPayload payload = command.getPayload();

        TripScheduleSnapshot snapshot = tripScheduleRepository.findSnapshot(tripId, payload.getTripScheduleId());
        if (snapshot == null) {
            throw new CustomException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
        }

        int updated = tripScheduleRepository.updateMemo(payload.getTripScheduleId(), payload.getMemo());
        if (updated == 0) {
            throw new CustomException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
        }

        TripScheduleWsDto changed = tripScheduleRepository.findWsDtoById(payload.getTripScheduleId());

        tripRepository.increaseRevision(tripId);
        Long newRevision = tripRepository.findRevisionById(tripId);

        publishAfterCommit(tripId, TripChangedEvent.builder()
                .eventType("SCHEDULE_UPDATED")
                .tripId(tripId)
                .revision(newRevision)
                .actorMemberId(memberId)
                .clientRequestId(command.getClientRequestId())
                .occurredAt(LocalDateTime.now())
                .payload(changed)
                .build());
    }

    @Transactional
    public void delete(Long tripId, Principal principal, TripEditCommand<DeleteSchedulePayload> command) {
        Long memberId = extractMemberId(principal);
        validateParticipant(tripId, memberId);
        validateRevision(tripId, command.getBaseRevision());

        DeleteSchedulePayload payload = command.getPayload();

        TripScheduleSnapshot snapshot = tripScheduleRepository.findSnapshot(tripId, payload.getTripScheduleId());
        if (snapshot == null) {
            throw new CustomException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
        }

        int deleted = tripScheduleRepository.deleteById(snapshot.getTripScheduleId());
        if (deleted == 0) {
            throw new CustomException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
        }

        compactAfterDelete(snapshot.getOldTripDayId(), snapshot.getSortOrder());

        tripRepository.increaseRevision(tripId);
        Long newRevision = tripRepository.findRevisionById(tripId);

        publishAfterCommit(tripId, TripChangedEvent.builder()
                .eventType("SCHEDULE_DELETED")
                .tripId(tripId)
                .revision(newRevision)
                .actorMemberId(memberId)
                .clientRequestId(command.getClientRequestId())
                .occurredAt(LocalDateTime.now())
                .payload(Map.of(
                        "tripScheduleId", snapshot.getTripScheduleId(),
                        "tripDayId", snapshot.getOldTripDayId(),
                        "deletedSortOrder", snapshot.getSortOrder()
                ))
                .build());
    }

    @Transactional
    public void reorder(Long tripId, Principal principal, TripEditCommand<ReorderSchedulesPayload> command) {
        Long memberId = extractMemberId(principal);
        validateParticipant(tripId, memberId);
        validateRevision(tripId, command.getBaseRevision());

        ReorderSchedulesPayload payload = command.getPayload();
        validateTripDayBelongsToTrip(tripId, payload.getTripDayId());

        List<Long> currentIds = tripScheduleRepository.findScheduleIdsByTripDayId(payload.getTripDayId());
        List<Long> requestedIds = payload.getOrderedScheduleIds();

        if (requestedIds == null || requestedIds.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (currentIds.size() != requestedIds.size()
                || !new HashSet<>(currentIds).equals(new HashSet<>(requestedIds))) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        tripScheduleRepository.bumpSortOrdersTemporarily(payload.getTripDayId(), TEMP_OFFSET);

        int sortOrder = 1;
        for (Long scheduleId : requestedIds) {
            int updated = tripScheduleRepository.updateSortOrder(scheduleId, sortOrder++);
            if (updated == 0) {
                throw new CustomException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
            }
        }

        List<TripScheduleWsDto> changed = tripScheduleRepository.findWsDtosByTripDayId(payload.getTripDayId());

        tripRepository.increaseRevision(tripId);
        Long newRevision = tripRepository.findRevisionById(tripId);

        publishAfterCommit(tripId, TripChangedEvent.builder()
                .eventType("SCHEDULE_REORDERED")
                .tripId(tripId)
                .revision(newRevision)
                .actorMemberId(memberId)
                .clientRequestId(command.getClientRequestId())
                .occurredAt(LocalDateTime.now())
                .payload(Map.of(
                        "tripDayId", payload.getTripDayId(),
                        "schedules", changed
                ))
                .build());
    }

    @Transactional
    public void move(Long tripId, Principal principal, TripEditCommand<MoveSchedulePayload> command) {
        Long memberId = extractMemberId(principal);
        validateParticipant(tripId, memberId);
        validateRevision(tripId, command.getBaseRevision());

        MoveSchedulePayload payload = command.getPayload();
        validateTripDayBelongsToTrip(tripId, payload.getTargetTripDayId());

        TripScheduleSnapshot snapshot = tripScheduleRepository.findSnapshot(tripId, payload.getTripScheduleId());
        if (snapshot == null) {
            throw new CustomException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
        }

        Long sourceTripDayId = snapshot.getOldTripDayId();
        Long targetTripDayId = payload.getTargetTripDayId();

        if (Objects.equals(sourceTripDayId, targetTripDayId)) {
            moveWithinSameDay(snapshot, payload);
        } else {
            moveAcrossDay(snapshot, payload);
        }

        TripScheduleWsDto changed = tripScheduleRepository.findWsDtoById(payload.getTripScheduleId());
        List<TripScheduleWsDto> sourceSchedules = tripScheduleRepository.findWsDtosByTripDayId(sourceTripDayId);
        List<TripScheduleWsDto> targetSchedules = tripScheduleRepository.findWsDtosByTripDayId(targetTripDayId);

        tripRepository.increaseRevision(tripId);
        Long newRevision = tripRepository.findRevisionById(tripId);

        publishAfterCommit(tripId, TripChangedEvent.builder()
                .eventType("SCHEDULE_MOVED")
                .tripId(tripId)
                .revision(newRevision)
                .actorMemberId(memberId)
                .clientRequestId(command.getClientRequestId())
                .occurredAt(LocalDateTime.now())
                .payload(Map.of(
                        "movedSchedule", changed,
                        "sourceTripDayId", sourceTripDayId,
                        "targetTripDayId", targetTripDayId,
                        "sourceSchedules", sourceSchedules,
                        "targetSchedules", targetSchedules
                ))
                .build());
    }

    private void moveWithinSameDay(TripScheduleSnapshot snapshot, MoveSchedulePayload payload) {
        Long tripDayId = snapshot.getOldTripDayId();
        Integer currentOrder = snapshot.getSortOrder();
        Integer targetOrder = payload.getTargetOrder();

        if (Objects.equals(currentOrder, targetOrder)) {
            return;
        }

        int maxOrder = tripScheduleRepository.countByTripDayId(tripDayId);
        validateTargetOrder(targetOrder, maxOrder);

        tripScheduleRepository.moveToTemporaryOrder(snapshot.getTripScheduleId(), TEMP_ORDER);

        if (targetOrder < currentOrder) {
            tripScheduleRepository.bumpIncreaseRange(tripDayId, targetOrder, currentOrder - 1, TEMP_OFFSET);
            tripScheduleRepository.normalizeIncreaseRange(
                    tripDayId,
                    targetOrder + TEMP_OFFSET,
                    currentOrder - 1 + TEMP_OFFSET,
                    TEMP_OFFSET
            );
        } else {
            tripScheduleRepository.bumpDecreaseRange(tripDayId, currentOrder + 1, targetOrder, TEMP_OFFSET);
            tripScheduleRepository.normalizeDecreaseRange(
                    tripDayId,
                    currentOrder + 1 + TEMP_OFFSET,
                    targetOrder + TEMP_OFFSET,
                    TEMP_OFFSET
            );
        }

        tripScheduleRepository.updateTripDayAndSortOrder(
                snapshot.getTripScheduleId(),
                tripDayId,
                targetOrder
        );
    }

    private void moveAcrossDay(TripScheduleSnapshot snapshot, MoveSchedulePayload payload) {
        Long sourceTripDayId = snapshot.getOldTripDayId();
        Long targetTripDayId = payload.getTargetTripDayId();

        int targetCount = tripScheduleRepository.countByTripDayId(targetTripDayId);
        int targetOrder = payload.getTargetOrder() == null ? targetCount + 1 : payload.getTargetOrder();
        validateTargetOrder(targetOrder, targetCount + 1);

        tripScheduleRepository.moveToTemporaryOrder(snapshot.getTripScheduleId(), TEMP_ORDER);

        compactAfterDelete(sourceTripDayId, snapshot.getSortOrder());

        tripScheduleRepository.bumpIncreaseFrom(targetTripDayId, targetOrder, TEMP_OFFSET);
        tripScheduleRepository.normalizeIncreaseFrom(targetTripDayId, targetOrder + TEMP_OFFSET, TEMP_OFFSET);

        tripScheduleRepository.updateTripDayAndSortOrder(
                snapshot.getTripScheduleId(),
                targetTripDayId,
                targetOrder
        );
    }

    private void compactAfterDelete(Long tripDayId, Integer deletedSortOrder) {
        tripScheduleRepository.bumpSortOrdersAfterDelete(tripDayId, deletedSortOrder, TEMP_OFFSET);
        tripScheduleRepository.normalizeSortOrdersAfterDelete(tripDayId, TEMP_OFFSET);
    }

    private Long extractMemberId(Principal principal) {
        if (principal == null) {
            throw new CustomException(ErrorCode.TRIP_ACCESS_DENIED);
        }

        if (!(principal instanceof Authentication authentication)) {
            throw new CustomException(ErrorCode.TRIP_ACCESS_DENIED);
        }

        Object principalObj = authentication.getPrincipal();
        if (!(principalObj instanceof SecurityMember member)) {
            throw new CustomException(ErrorCode.TRIP_ACCESS_DENIED);
        }

        return member.getMemberId();
    }

    private void validateParticipant(Long tripId, Long memberId) {
        if (!tripPermissionRepository.isTripParticipant(tripId, memberId)) {
            throw new CustomException(ErrorCode.TRIP_ACCESS_DENIED);
        }
    }

    private void validateTripDayBelongsToTrip(Long tripId, Long tripDayId) {
        if (!tripScheduleRepository.existsTripDayInTrip(tripId, tripDayId)) {
            throw new CustomException(ErrorCode.TRIP_DAY_NOT_FOUND);
        }
    }

    private void validateRevision(Long tripId, Long baseRevision) {
        Long currentRevision = tripRepository.findRevisionById(tripId);
        if (!Objects.equals(currentRevision, baseRevision)) {
            throw new CustomException(ErrorCode.TRIP_REVISION_CONFLICT);
        }
    }

    private void validateTargetOrder(Integer targetOrder, Integer maxInclusive) {
        if (targetOrder == null || targetOrder < 1 || targetOrder > maxInclusive) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void publishAfterCommit(Long tripId, Object eventPayload) {
        eventPublisher.publishEvent(new TripChangedAfterCommitEvent(tripId, eventPayload));
    }
}