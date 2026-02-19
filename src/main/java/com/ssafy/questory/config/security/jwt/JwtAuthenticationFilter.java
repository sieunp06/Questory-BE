package com.ssafy.questory.config.security.jwt;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.extractAllClaims(token, JwtService.TokenType.ACCESS);
            String email = claims.getSubject();

            if (email == null || email.isBlank()) {
                request.setAttribute("auth_error", "TOKEN_INVALID_SUBJECT");
                authenticationEntryPoint.commence(request, response, null);
                return;
            }

            UserDetails userDetails = jwtUserDetailsService.loadUserByEmail(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            request.setAttribute("auth_error", "TOKEN_EXPIRED");
            authenticationEntryPoint.commence(request, response, null);
        } catch (io.jsonwebtoken.security.SignatureException | SecurityException e) {
            request.setAttribute("auth_error", "TOKEN_SIGNATURE_INVALID");
            authenticationEntryPoint.commence(request, response, null);
        } catch (MalformedJwtException e) {
            request.setAttribute("auth_error", "TOKEN_MALFORMED");
            authenticationEntryPoint.commence(request, response, null);
        } catch (UnsupportedJwtException e) {
            request.setAttribute("auth_error", "TOKEN_UNSUPPORTED");
            authenticationEntryPoint.commence(request, response, null);
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute("auth_error", "TOKEN_INVALID");
            authenticationEntryPoint.commence(request, response, null);
        }
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;

        String token = authHeader.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
