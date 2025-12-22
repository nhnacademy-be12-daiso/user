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

import com.nhnacademy.user.dto.payco.PaycoLoginResponse;
import com.nhnacademy.user.dto.payco.PaycoSignUpRequest;
import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.BirthdayUserResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "유저 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // POST /api/users/payco/login
    @PostMapping("/payco/login")
    @Operation(summary = "Payco 로그인/회원가입")
    public ResponseEntity<PaycoLoginResponse> paycoLogin(@Valid @RequestBody PaycoSignUpRequest request) {
        PaycoLoginResponse response = userService.findOrCreatePaycoUser(request);
        HttpStatus status = response.isNewUser() ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }

    // GET /api/users/birthday
    @Operation(summary = "생일 월로 사용자 조회", description = "특정 월이 생일인 사용자 목록 조회")
    @GetMapping("/birthday")
    public ResponseEntity<List<BirthdayUserResponse>> getBirthdayUsers(
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userCreatedId").ascending());
        Slice<BirthdayUserResponse> slice = userService.findByBirthdayMonth(month, pageable);
        return ResponseEntity.ok(slice.getContent());
    }


    // POST /api/users/signup
    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 유저")
    })
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignupRequest request) {
        userService.signUp(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/users/me
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "탈퇴한 계정"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저"),
    })
    public ResponseEntity<UserResponse> getMyInfo(@RequestHeader(name = "X-User-Id") Long userCreatedId) { // 사용자 정보 조회
        UserResponse userInfo = userService.getUserInfo(userCreatedId);

        return ResponseEntity.status(HttpStatus.OK).body(userInfo);
    }

    // PUT /api/users/me
    @PutMapping("/me")
    @Operation(summary = "내 정보 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 유저")
    })
    public ResponseEntity<Void> modifyMyInfo(@RequestHeader(name = "X-User-Id") Long userCreatedId,
                                             @Valid @RequestBody UserModifyRequest request) { // 사용자 정보 수정
        userService.modifyUserInfo(userCreatedId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // PUT /api/users/me/password
    @Operation(summary = "비밀번호 변경")
    @PutMapping("/me/password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    })
    public ResponseEntity<Void> modifyMyPassword(@RequestHeader(name = "X-User-Id") Long userCreatedId,
                                                 @Valid @RequestBody PasswordModifyRequest request) { // 사용자 비밀번호 수정
        userService.modifyUserPassword(userCreatedId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // DELETE /api/users/me
    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    public ResponseEntity<Void> withdrawMyAccount(@RequestHeader(name = "X-User-Id") Long userCreatedId) {
        userService.withdrawUser(userCreatedId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
