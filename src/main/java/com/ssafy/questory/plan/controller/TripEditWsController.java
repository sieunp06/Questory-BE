package com.ssafy.questory.plan.controller;

import com.ssafy.questory.plan.dto.command.TripEditCommand;
import com.ssafy.questory.plan.dto.payload.AddSchedulePayload;
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

    @MessageMapping("/trip/{tripId}/schedule/add")
    public void addSchedule(@DestinationVariable Long tripId,
                            Principal principal,
                            TripEditCommand<AddSchedulePayload> command) {
        tripCollaborationService.addSchedule(tripId, principal, command);
    }
}
