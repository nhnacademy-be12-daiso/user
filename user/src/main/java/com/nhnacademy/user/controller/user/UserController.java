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

package com.nhnacademy.user.controller.user;

import com.nhnacademy.user.dto.request.LoginRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.properties.JwtProperties;
import com.nhnacademy.user.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    // JWT 발급과 무효화 흐름을 담당하는 인증 전용 컨트롤러, 요청을 UserService로 전달하는 역할만 담당
    // 실제 인증은 Security와 JWT 필터가 처리

    private final UserService userService;

    private final JwtProperties jwtProperties;

    // POST /users/signup
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignupRequest request) {
        userService.signUp(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // POST /users/login
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        // UserService에서 사용자 인증 후 JWT 생성하여 문자열로 반환
        String token = userService.login(request);

        // 토큰을 헤더에 담아 클라이언트로 반환
        return ResponseEntity.status(HttpStatus.OK).header(jwtProperties.getHeader(), token).build();
    }

    // POST /users/logout
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        // 클라이언트가 현재 보유 중인 JWT를 Authorization 헤더로 전송
        // .logout 내부: redis에 해당 토큰을 블랙리스트로 저장, 이후 해당 토큰으로 요청이 오면 JWTAuthenticationFilter에서 차단됨
        userService.logout(authHeader);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(Authentication authentication) {
        String loginId = (String) authentication.getPrincipal();

        UserResponse userInfo = userService.getUserInfo(loginId);

        return ResponseEntity.status(HttpStatus.OK).body(userInfo);
    }

}
