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
import com.nhnacademy.user.dto.request.PasswordModifyRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    // GET /users/me
    @Operation(summary = "내 정보 조회", description = "JWT 기반으로 인증된 사용자의 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@RequestHeader(name = "X-USER-ID") String loginId) {
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
    public ResponseEntity<Void> modifyMyInfo(@RequestHeader(name = "X-USER-ID") String loginId,
                                             @Valid @RequestBody UserModifyRequest request) {
        // 사용자 정보 수정
        userService.modifyUserInfo(loginId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // PATCH /users/me/password
    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (현재 비밀번호 불일치)", content = @Content(schema = @Schema(hidden = true)))})
    @PutMapping("/me/password")
    public ResponseEntity<Void> modifyMyPassword(@RequestHeader(name = "X-USER-ID") String loginId,
                                                 @Valid @RequestBody PasswordModifyRequest request) {
        // 사용자 비밀번호 수정
        userService.modifyUserPassword(loginId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // DELETE /users/me
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 '탈퇴(WITHDRAWN)' 상태로 변경하고, 현재 토큰을 무효화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))})
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawMyAccount(@RequestHeader(name = "X-USER-ID") String loginId,
                                                  @RequestHeader("Authorization") String token) {
        userService.withdrawUser(loginId, token);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // POST /users/activate?loginId={loginId}
    @Operation(summary = "휴면 계정 활성화", description = "휴면 상태의 계정을 활성화합니다. (인증 절차 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계정 활성화 성공"),
            @ApiResponse(responseCode = "404", description = "계정을 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))})
    @PostMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam("loginId") String loginId) {
        // 임시!!!! 나중에 Dooray Message Sender로 인증해야함.!
        userService.activeUser(loginId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
