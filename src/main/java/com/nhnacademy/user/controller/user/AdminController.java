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

import com.nhnacademy.user.dto.request.UserGradeRequest;
import com.nhnacademy.user.dto.request.UserStatusRequest;
import com.nhnacademy.user.dto.response.UserDetailResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.service.user.AdminService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;

    // GET /api/admin/users
    @Operation(summary = "전체 회원 목록 조회")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getAllUsers(pageable));
    }

    // GET /api/admin/users/{userCreatedId}
    @Operation(summary = "특정 회원 상세 조회")
    @GetMapping("/{userCreatedId}")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable Long userCreatedId) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getUserDetail(userCreatedId));
    }

    // PUT /api/admin/users/{userCreatedId}/status
    @Operation(summary = "회원 상태 변경")
    @PutMapping("/{userCreatedId}/status")
    public ResponseEntity<Void> modifyUserStatus(@RequestHeader("X-User-Id") Long adminId,
                                                 @PathVariable Long userCreatedId,
                                                 @RequestBody UserStatusRequest request) {
        adminService.modifyUserStatus(adminId, userCreatedId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // PUT /api/admin/users/{userCreatedId}/grade
    @Operation(summary = "회원 등급 변경")
    @PutMapping("/{userCreatedId}/grade")
    public ResponseEntity<Void> modifyUserGrade(@RequestHeader("X-User-Id") Long adminId,
                                                @PathVariable Long userCreatedId,
                                                @RequestBody UserGradeRequest request) {
        adminService.modifyUserGrade(adminId, userCreatedId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
