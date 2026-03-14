package com.ssafy.questory.plan.controller;

import com.ssafy.questory.plan.dto.command.TripEditCommand;
import com.ssafy.questory.plan.dto.payload.AddSchedulePayload;
import com.ssafy.questory.plan.dto.payload.DeleteSchedulePayload;
import com.ssafy.questory.plan.dto.payload.ReorderSchedulesPayload;
import com.ssafy.questory.plan.dto.payload.UpdateMemoPayload;
import com.ssafy.questory.plan.service.TripCollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class TripEditWsController {
    private final TripCollaborationService tripCollaborationService;

    @MessageMapping("/trips/{tripId}/schedule/add")
    public void add(@DestinationVariable Long tripId,
                            Principal principal,
                            TripEditCommand<AddSchedulePayload> command) {
        tripCollaborationService.add(tripId, principal, command);
    }

    @MessageMapping("/trips/{tripId}/schedule/update-memo")
    public void updateMemo(@DestinationVariable Long tripId,
                           Principal principal,
                           TripEditCommand<UpdateMemoPayload> command) {
        tripCollaborationService.updateMemo(tripId, principal, command);
    }

    @MessageMapping("/trips/{tripId}/schedule/delete")
    public void delete(@DestinationVariable Long tripId,
                               Principal principal,
                               TripEditCommand<DeleteSchedulePayload> command) {
        tripCollaborationService.delete(tripId, principal, command);
    }

    @MessageMapping("/trips/{tripId}/schedule/reorder")
    public void reorder(@DestinationVariable Long tripId,
                        Principal principal,
                        TripEditCommand<ReorderSchedulesPayload> command) {
        tripCollaborationService.reorder(tripId, principal, command);
    }
}
