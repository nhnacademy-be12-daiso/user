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
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.properties.JwtProperties;
import com.nhnacademy.user.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 API", description = "회원가입, 로그인, 로그아웃, 내 정보 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    // JWT 발급과 무효화 흐름을 담당하는 인증 전용 컨트롤러, 요청을 UserService로 전달하는 역할만 담당
    // 실제 인증은 Security와 JWT 필터가 처리

    private final UserService userService;

    private final JwtProperties jwtProperties;

    // POST /users/signup
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "중복된 아이디", content = @Content(schema = @Schema(hidden = true)))})
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignupRequest request) {
        userService.signUp(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // POST /users/login
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 헤더에 담아 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 — Authorization 헤더로 JWT 반환"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (아이디/비밀번호 불일치)", content = @Content(schema = @Schema(hidden = true)))})
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        // UserService에서 사용자 인증 후 JWT 생성하여 문자열로 반환
        String token = userService.login(request);

        // 토큰을 헤더에 담아 클라이언트로 반환
        return ResponseEntity.status(HttpStatus.OK).header(jwtProperties.getHeader(), token).build();
    }

    // POST /users/logout
    @Operation(summary = "로그아웃", description = "클라이언트 JWT를 무효화(블랙리스트 등록)합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "토큰 없음 또는 인증 실패", content = @Content(schema = @Schema(hidden = true)))})
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        // 클라이언트가 현재 보유 중인 JWT를 Authorization 헤더로 전송
        // .logout 내부: redis에 해당 토큰을 블랙리스트로 저장, 이후 해당 토큰으로 요청이 오면 JWTAuthenticationFilter에서 차단됨
        userService.logout(authHeader);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // GET /users/me
    @Operation(summary = "내 정보 조회", description = "JWT 기반으로 인증된 사용자의 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(Authentication authentication) {
        // Authentication 객체에서 로그인 아이디(principal) 가져옴
        String loginId = (String) authentication.getPrincipal();

        // 사용자 정보 조회
        UserResponse userInfo = userService.getUserInfo(loginId);

        return ResponseEntity.status(HttpStatus.OK).body(userInfo);
    }

    // PATCH /users/me
    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 이름, 연락처, 이메일, 생일을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(hidden = true)))})
    @PatchMapping("/me")
    public ResponseEntity<Void> modifyMyInfo(Authentication authentication,
                                             @Valid @RequestBody UserModifyRequest request) {
        // Authentication 객체에서 로그인 아이디(principal) 가져옴
        String loginId = (String) authentication.getPrincipal();

        // 사용자 정보 수정
        userService.modifyUserInfo(loginId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
