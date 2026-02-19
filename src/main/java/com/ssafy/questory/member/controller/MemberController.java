package com.ssafy.questory.member.controller;

import com.ssafy.questory.member.dto.request.MemberRegisterRequestDto;
import com.ssafy.questory.member.dto.response.MemberResponseDto;
import com.ssafy.questory.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/member")
@RestController
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<MemberResponseDto> register(@Valid @RequestBody MemberRegisterRequestDto memberRegistRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.register(memberRegistRequestDto));
    }
}
