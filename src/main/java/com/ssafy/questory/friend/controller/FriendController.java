package com.ssafy.questory.friend.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.friend.dto.FriendListResponseDto;
import com.ssafy.questory.friend.dto.FriendRequestResponseDto;
import com.ssafy.questory.friend.service.FriendService;
import com.ssafy.questory.mail.dto.request.MemberEmailRequestDto;
import com.ssafy.questory.member.domain.SecurityMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @GetMapping
    public ResponseEntity<List<FriendListResponseDto>> getFriends(
            @AuthenticationPrincipal SecurityMember member) {
        return ResponseEntity.ok(friendService.getFriends(member));
    }

    @GetMapping("/request")
    public ResponseEntity<List<FriendRequestResponseDto>> getFriendRequestInfo(
            @AuthenticationPrincipal SecurityMember member) {
        return ResponseEntity.status(HttpStatus.OK).body(friendService.getFriendRequests(member));
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> request(
            @AuthenticationPrincipal SecurityMember member,
            @Valid @RequestBody MemberEmailRequestDto dto) {
        friendService.request(member, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("친구 요청이 전송되었습니다."));
    }

    @GetMapping("/request/sent")
    public ResponseEntity<List<FriendRequestResponseDto>> getSentFriendRequests(
            @AuthenticationPrincipal SecurityMember member) {
        return ResponseEntity.ok(friendService.getSentFriendRequests(member));
    }

    @PostMapping("/request/{friendRequestId}/accept")
    public ResponseEntity<ApiResponse<Void>> accept(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long friendRequestId) {
        friendService.acceptRequest(member, friendRequestId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok("친구 요청을 수락했습니다."));
    }

    @PostMapping("/request/{friendRequestId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long friendRequestId) {
        friendService.rejectRequest(member, friendRequestId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok("친구 요청을 거절했습니다."));
    }

    @PostMapping("/request/{friendRequestId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long friendRequestId
    ) {
        friendService.cancelRequest(member, friendRequestId);
        return ResponseEntity.ok(ApiResponse.ok("친구 요청을 취소했습니다."));
    }
}
