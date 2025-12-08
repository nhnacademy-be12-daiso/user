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

import com.nhnacademy.user.dto.request.AccountStatusRequest;
import com.nhnacademy.user.dto.request.UserGradeRequest;
import com.nhnacademy.user.dto.request.UserSearchCriteria;
import com.nhnacademy.user.dto.response.UserDetailResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.service.user.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;

    // GET /api/admin/users
    @GetMapping
    @Operation(summary = "전체 회원 목록 조회")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault Pageable pageable,
                                                          @RequestParam(required = false) UserSearchCriteria criteria) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getAllUsers(pageable, criteria));
    }

    // GET /api/admin/users/{userCreatedId}
    @GetMapping("/{userCreatedId}")
    @Operation(summary = "특정 회원 상세 조회")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable Long userCreatedId) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getUserDetail(userCreatedId));
    }

    // PUT /api/admin/users/{userCreatedId}/status
    @PutMapping("/{userCreatedId}/status")
    @Operation(summary = "회원 상태 변경")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    public ResponseEntity<Void> modifyUserStatus(@RequestHeader("X-User-Id") Long adminId,
                                                 @PathVariable Long userCreatedId,
                                                 @RequestBody AccountStatusRequest request) {
        adminService.modifyUserStatus(adminId, userCreatedId, request);
        log.info("관리자 [{}] - 회원 [{}] 상태({}) 변경", adminId, userCreatedId, request.statusName());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // PUT /api/admin/users/{userCreatedId}/grade
    @PutMapping("/{userCreatedId}/grade")
    @Operation(summary = "회원 등급 변경")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    public ResponseEntity<Void> modifyUserGrade(@RequestHeader("X-User-Id") Long adminId,
                                                @PathVariable Long userCreatedId,
                                                @RequestBody UserGradeRequest request) {
        adminService.modifyUserGrade(adminId, userCreatedId, request);
        log.info("관리자 [{}] - 회원 [{}] 등급({}) 변경", adminId, userCreatedId, request.gradeName());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
