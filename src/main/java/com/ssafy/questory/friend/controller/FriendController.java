package com.ssafy.questory.friend.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.friend.service.FriendService;
import com.ssafy.questory.mail.dto.request.MemberEmailRequestDto;
import com.ssafy.questory.member.domain.SecurityMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> request(
            @AuthenticationPrincipal SecurityMember member,
            @Valid @RequestBody MemberEmailRequestDto dto) {
        friendService.request(member, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("친구 요청이 전송되었습니다."));
    }
}
