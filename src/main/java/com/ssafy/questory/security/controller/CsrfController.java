package com.ssafy.questory.security.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/csrf")
@RestController
public class CsrfController {

    @GetMapping()
    public CsrfToken csrfToken(CsrfToken token) {
        return token;
    }
}
