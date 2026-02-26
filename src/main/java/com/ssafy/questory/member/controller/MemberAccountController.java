package com.ssafy.questory.member.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.dto.response.MemberInfoResponseDto;
import com.ssafy.questory.member.service.MemberAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class MemberAccountController {
    private final MemberAccountService memberAccountService;

    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponseDto> getMemberInfo(
            @AuthenticationPrincipal SecurityMember member) {
        return ResponseEntity.status(HttpStatus.OK).body(memberAccountService.getUserInfo(member));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal SecurityMember member) {
        memberAccountService.withdraw(member);
        return ResponseEntity.ok().body(ApiResponse.ok("탈퇴가 완료되었습니다."));
    }

    @GetMapping()
    public ResponseEntity<List<MemberInfoResponseDto>> search(
            @AuthenticationPrincipal SecurityMember member,
            @RequestParam("email") String email) {
        return ResponseEntity.ok().body(memberAccountService.search(member, email));
    }
}
