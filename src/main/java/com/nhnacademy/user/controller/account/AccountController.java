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

package com.nhnacademy.user.controller.account;

import com.nhnacademy.user.dto.request.FindLoginIdRequest;
import com.nhnacademy.user.dto.request.FindPasswordRequest;
import com.nhnacademy.user.service.account.AccountService;
import com.nhnacademy.user.service.account.FindAccountService;
import com.nhnacademy.user.service.message.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "계정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AccountController {

    private final AccountService accountService;

    private final VerificationService verificationService;

    private final FindAccountService findAccountService;

    // POST /api/users/activate/send-code
    @PostMapping("/activate/send-code")
    @Operation(summary = "휴면 해제 인증코드 발송")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "휴면 상태가 아닌 계정"),
            @ApiResponse(responseCode = "401", description = "올바르지 않은 코드 입력"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    })
    public ResponseEntity<Void> sendVerifyCode(@RequestHeader("X-User-Id") Long userCreatedId) {
        log.info("[AccountController] 휴면 해제 인증코드 발송 요청 - userCreatedId: {}", userCreatedId);

        try {
            verificationService.sendCode(userCreatedId);
            log.info("[AccountController] 휴면 해제 인증코드 발송 성공 - userCreatedId: {}", userCreatedId);

            return ResponseEntity.status(HttpStatus.OK).build();

        } catch (Exception e) {
            log.error("[AccountController] 휴면 해제 인증코드 발송 실패 - userCreatedId: {}, error: {}",
                    userCreatedId, e.getMessage(), e);
            throw e;
        }
    }

    // POST /api/users/activate?code={code}
    @PostMapping("/activate")
    @Operation(summary = "휴면 계정 활성화 (인증코드 필수)")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "올바르지 않은 코드 입력"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    })
    public ResponseEntity<Void> activateAccount(@RequestHeader("X-User-Id") Long userCreatedId,
                                                @RequestParam String code) {
        verificationService.verifyCode(userCreatedId, code);

        accountService.activeUser(userCreatedId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // POST /api/users/find-id
    @PostMapping("/find-id")
    @Operation(summary = "아이디 찾기")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    public ResponseEntity<String> findLoginId(@RequestBody FindLoginIdRequest request) {
        String maskedId = findAccountService.findLoginId(request);

        return ResponseEntity.status(HttpStatus.OK).body(maskedId);
    }

    // POST /api/users/find-password
    @PostMapping("/find-password")
    @Operation(summary = "비밀번호 찾기 (임시 비밀번호 발급)")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    public ResponseEntity<Void> findPassword(@RequestBody FindPasswordRequest request) {
        findAccountService.createTemporaryPassword(request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/check-id")
    @Operation(summary = "아이디 중복 확인")
    public ResponseEntity<Boolean> checkLoginId(@RequestParam String loginId) {
        boolean isExist = accountService.existsLoginId(loginId);

        return ResponseEntity.status(HttpStatus.OK).body(isExist);
    }

}
