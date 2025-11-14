/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2025. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.user.config.filter;

import com.nhnacademy.user.properties.JwtProperties;
import com.nhnacademy.user.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 한 요청 당 딱 한 번만 실행, JWT 전용 인증 필터
// 이 필터가 있기 때문에 컨트롤러에서 JWT 파싱, 토큰 검증, 토큰 만료 여부, 로그아웃 여부, 인증/인가 처리를 신경쓰지 않아도 됨

    private final JwtProperties jwtProperties;

    private final JwtUtil jwtUtil;

    // Key 값과 Value 값을 문자열로만 관리할 때 StringRedisTemplate
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 요청 헤더에서 JWT 꺼냄
        String header = request.getHeader(jwtProperties.getHeader());

        if (header == null || !header.startsWith(jwtProperties.getTokenPrefix() + " ")) {   // 헤더가 없거나 형식이 맞지 않으면
            filterChain.doFilter(request, response);

            return;     // 인증할 필요 없음, 그냥 다음 필터로 넘어감
        }

        // 헤더에서 Prefix 제거 후 JWT 추출
        String token = header.substring(jwtProperties.getTokenPrefix().length() + 1);

        // redis로 블랙리스트 확인
        // key: "<token>" / value: "logout"
        // 로그아웃 할 때 이런 식으로 저장
        String isBlacklisted = stringRedisTemplate.opsForValue().get(token);

        // 만약 redis에 토큰이 logout으로 등록되어 있다면 (블랙리스트라면, 이미 로그아웃된 토큰이라면)
        if (isBlacklisted != null && isBlacklisted.equals("logout")) {
            // 유효한 토큰으로 인정하지 않고 그냥 필터 통과 (비로그인 상태로 처리)
            filterChain.doFilter(request, response);

            return;
        }

        if (jwtUtil.isTokenValid(token)) {
            String loginId = jwtUtil.getLoginId(token);
            String role = jwtUtil.getRole(token);

            UsernamePasswordAuthenticationToken authenticationToken =
                    // principal = loginId, credentials = null(이미 인증된 패스워드), authorities = ROLE_ADMIN || ROLE_USER
                    new UsernamePasswordAuthenticationToken(loginId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            // SecurityContextHolder에 인증 정보 저장, 컨트롤러/서비스에서 인증된 사용자로 인식됨 (유저 정보 접근 가능)
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 다음 필터 실행
        filterChain.doFilter(request, response);
    }

}
