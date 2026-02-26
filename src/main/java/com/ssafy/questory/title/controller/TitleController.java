package com.ssafy.questory.title.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.title.dto.request.AcquireTitleRequestDto;
import com.ssafy.questory.title.dto.request.UpdateRepresentativeTitleRequestDto;
import com.ssafy.questory.title.dto.response.TitleResponseDto;
import com.ssafy.questory.title.service.TitleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account/title")
public class TitleController {
    private final TitleService titleService;

    @GetMapping()
    public ResponseEntity<List<TitleResponseDto>> getMyTitles(
            @AuthenticationPrincipal SecurityMember member) {
        return ResponseEntity.status(HttpStatus.OK).body(titleService.getMyTitles(member));
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<Void>> acquireTitle(
            @AuthenticationPrincipal SecurityMember member,
            @Valid @RequestBody AcquireTitleRequestDto dto) {
        titleService.acquireTitle(member, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("칭호가 추가되었습니다.ㅊ"));
    }

    @PatchMapping()
    public ResponseEntity<ApiResponse<Void>> updateRepresentativeTitle(
            @AuthenticationPrincipal SecurityMember member,
            @Valid @RequestBody UpdateRepresentativeTitleRequestDto dto) {
        titleService.updateRepresentativeTitle(member, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok("대표 칭호 변경이 완료되었습니다."));
    }
}
