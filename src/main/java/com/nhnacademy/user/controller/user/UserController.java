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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@Tag(name = "유저 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/payco/login")
    @Operation(summary = "Payco 로그인/회원가입")
    public ResponseEntity<PaycoLoginResponse> paycoLogin(@Valid @RequestBody PaycoSignUpRequest request) {
        PaycoLoginResponse response = userService.findOrCreatePaycoUser(request);
        HttpStatus status = response.isNewUser() ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "생일 월로 사용자 조회", description = "특정 월이 생일인 사용자 목록 조회")
    @GetMapping("/birthday")
    public ResponseEntity<List<BirthdayUserResponse>> getBirthdayUsers(
            @RequestParam int month,
            @RequestParam(defaultValue = "0") long lastSeenId,
            @RequestParam(defaultValue = "1000") int size
    ) {
        Pageable pageable = PageRequest.of(0, size); //  offset=0 고정
        List<BirthdayUserResponse> users =
                userService.findByBirthdayMonthAfter(month, 1L, lastSeenId, pageable);

        return ResponseEntity.ok(users);
    }


    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    @ApiResponse(responseCode = "201", description = "회원가입 완료")
    @ApiResponse(responseCode = "400", description = "잘못된 포인트 입력 값")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 포인트 정책")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 등급")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 상태")
    @ApiResponse(responseCode = "409", description = "이미 존재하는 로그인 아이디")
    @ApiResponse(responseCode = "409", description = "이미 존재하는 연락처")
    @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignupRequest request) {
        userService.signUp(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(request.loginId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    @ApiResponse(responseCode = "200", description = "내 정보 조회 완료")
    @ApiResponse(responseCode = "403", description = "이미 탈퇴한 계정")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    public ResponseEntity<UserResponse> getMyInfo(@RequestHeader(name = "X-User-Id") Long userCreatedId) {
        return ResponseEntity.ok().body(userService.getUserInfo(userCreatedId));
    }

    @PutMapping("/me")
    @Operation(summary = "내 정보 수정")
    @ApiResponse(responseCode = "200", description = "내 정보 수정 완료")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    @ApiResponse(responseCode = "409", description = "이미 존재하는 연락처")
    @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    public ResponseEntity<Void> modifyMyInfo(@RequestHeader(name = "X-User-Id") Long userCreatedId,
                                             @Valid @RequestBody UserModifyRequest request) {
        userService.modifyUserInfo(userCreatedId, request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경")
    @PutMapping("/me/password")
    @ApiResponse(responseCode = "200", description = "비밀번호 변경 완료")
    @ApiResponse(responseCode = "400", description = "비밀번호 불일치")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    public ResponseEntity<Void> modifyMyPassword(@RequestHeader(name = "X-User-Id") Long userCreatedId,
                                                 @Valid @RequestBody PasswordModifyRequest request) {
        userService.modifyAccountPassword(userCreatedId, request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴 완료")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 상태")
    public ResponseEntity<Void> withdrawMyAccount(@RequestHeader(name = "X-User-Id") Long userCreatedId) {
        userService.withdrawUser(userCreatedId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

