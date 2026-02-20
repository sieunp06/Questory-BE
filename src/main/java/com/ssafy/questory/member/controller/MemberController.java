package com.ssafy.questory.member.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.member.dto.request.LoginRequestDto;
import com.ssafy.questory.member.dto.request.RegisterRequestDto;
import com.ssafy.questory.member.dto.response.MemberResponseDto;
import com.ssafy.questory.member.dto.response.TokenResponseDto;
import com.ssafy.questory.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RequiredArgsConstructor
@RequestMapping("/api/member")
@RestController
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<MemberResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequestDto dto) {
        TokenResponseDto tokens = memberService.login(dto);
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.ok("로그인에 성공했습니다."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Refresh token이 존재하지 않습니다.."));
        }

        String accessToken = memberService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(ApiResponse.ok("Access Token 재발급에 성공했습니다."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
                .body(ApiResponse.ok("로그아웃에 성공했습니다."));
    }
}
