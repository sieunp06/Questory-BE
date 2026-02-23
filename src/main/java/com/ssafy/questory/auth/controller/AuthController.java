package com.ssafy.questory.auth.controller;

import com.ssafy.questory.auth.dto.TicketExchangeRequestDto;
import com.ssafy.questory.auth.service.AuthService;
import com.ssafy.questory.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<Void>> exchange(@Valid @RequestBody TicketExchangeRequestDto dto) {
        String accessToken = authService.exchangeTicket(dto.ticket());
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(ApiResponse.ok("Access Token 발급에 성공했습니다."));
    }
}
