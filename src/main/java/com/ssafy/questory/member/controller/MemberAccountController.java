package com.ssafy.questory.member.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.service.MemberAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class MemberAccountController {
    private final MemberAccountService memberAccountService;

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal SecurityMember member) {
        memberAccountService.withdraw(member);
        return ResponseEntity.ok().body(ApiResponse.ok("탈퇴가 완료되었습니다."));
    }
}
