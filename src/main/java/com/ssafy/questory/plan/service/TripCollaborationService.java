package com.ssafy.questory.plan.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.plan.dto.command.TripEditCommand;
import com.ssafy.questory.plan.dto.event.TripChangedAfterCommitEvent;
import com.ssafy.questory.plan.dto.event.TripChangedEvent;
import com.ssafy.questory.plan.dto.payload.AddSchedulePayload;
import com.ssafy.questory.plan.dto.payload.DeleteSchedulePayload;
import com.ssafy.questory.plan.dto.payload.UpdateMemoPayload;
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
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TripCollaborationService {
    private final TripRepository tripRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final TripPermissionRepository tripPermissionRepository;

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
        TripScheduleWsDto created = tripScheduleRepository.findWsDtoById(insertCommand.getTripScheduleId());

        Long newRevision = tripRepository.increaseRevision(tripId);

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

        tripScheduleRepository.updateMemo(payload.getTripScheduleId(), payload.getMemo());

        TripScheduleWsDto changed = tripScheduleRepository.findWsDtoById(payload.getTripScheduleId());
        Long newRevision = tripRepository.increaseRevision(tripId);

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

        tripScheduleRepository.deleteById(snapshot.getTripScheduleId());
        tripScheduleRepository.decreaseSortOrdersAfterDelete(snapshot.getOldTripDayId(), snapshot.getSortOrder());

        Long newRevision = tripRepository.increaseRevision(tripId);

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

    private void validateRevision(Long tripId, Long baseRevision) {
        Long currentRevision = tripRepository.findRevisionById(tripId);
        if (!Objects.equals(currentRevision, baseRevision)) {
            throw new CustomException(ErrorCode.TRIP_REVISION_CONFLICT);
        }
    }

    private void validateTripDayBelongsToTrip(Long tripId, Long tripDayId) {
        if (!tripScheduleRepository.existsTripDayInTrip(tripId, tripDayId)) {
            throw new CustomException(ErrorCode.TRIP_DAY_NOT_FOUND);
        }
    }

    private void publishAfterCommit(Long tripId, Object eventPayload) {
        eventPublisher.publishEvent(new TripChangedAfterCommitEvent(tripId, eventPayload));
    }
}
