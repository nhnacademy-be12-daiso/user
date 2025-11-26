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

import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.service.message.VerificationService;
import com.nhnacademy.user.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "유저 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final VerificationService verificationService;

    // POST /api/users/signup
    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignupRequest request) {
        userService.signUp(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/users/me
    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@RequestHeader(name = "X-User-Id") Long userCreatedId) {
        // 사용자 정보 조회
        UserResponse userInfo = userService.getUserInfo(userCreatedId);

        return ResponseEntity.status(HttpStatus.OK).body(userInfo);
    }

    // PATCH /api/users/me
    @Operation(summary = "내 정보 수정")
    @PatchMapping("/me")
    public ResponseEntity<Void> modifyMyInfo(@RequestHeader(name = "X-User-Id") Long userCreatedId,
                                             @Valid @RequestBody UserModifyRequest request) {
        // 사용자 정보 수정
        userService.modifyUserInfo(userCreatedId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // PATCH /api/users/me/password
    @Operation(summary = "비밀번호 변경")
    @PutMapping("/me/password")
    public ResponseEntity<Void> modifyMyPassword(@RequestHeader(name = "X-User-Id") Long userCreatedId,
                                                 @Valid @RequestBody PasswordModifyRequest request) {
        // 사용자 비밀번호 수정
        userService.modifyUserPassword(userCreatedId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // DELETE /api/users/me
    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawMyAccount(@RequestHeader(name = "X-User-Id") Long userCreatedId) {
        userService.withdrawUser(userCreatedId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // POST /api/users/activate/send-code
    @Operation(summary = "휴면 해제 인증코드 발송")
    @PostMapping("/activate/send-code")
    public ResponseEntity<Void> sendVerifyCode(@RequestHeader("X-User-Id") Long userCreatedId) {
        verificationService.sendCode(userCreatedId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // POST /api/users/activate?code={code}
    @Operation(summary = "휴면 계정 활성화 (인증코드 필수)")
    @PostMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestHeader("X-User-Id") Long userCreatedId,
                                                @RequestParam String code) {
        verificationService.verifyCode(userCreatedId, code);

        userService.activeUser(userCreatedId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
