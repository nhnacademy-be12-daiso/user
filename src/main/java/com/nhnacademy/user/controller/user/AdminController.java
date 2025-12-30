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
import com.nhnacademy.user.dto.response.UserDetailResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.dto.search.UserSearchCriteria;
import com.nhnacademy.user.service.user.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;

    // GET /api/admin/users
    // GET /api/admin/users?keyword={keyword}
    @GetMapping
    @Operation(summary = "전체 회원 목록 조회")
    @ApiResponse(responseCode = "200", description = "전체 회원 목록 조회 완료")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault Pageable pageable,
                                                          @RequestParam(name = "keyword", required = false)
                                                          String keyword) {
        return ResponseEntity.ok().body(adminService.getAllUsers(pageable, new UserSearchCriteria(keyword)));
    }

    // GET /api/admin/users/{userCreatedId}
    @GetMapping("/{userCreatedId}")
    @Operation(summary = "특정 회원 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "특정 회원 상세 조회 완료"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    })
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable Long userCreatedId) {
        return ResponseEntity.ok().body(adminService.getUserDetail(userCreatedId));
    }

    // PUT /api/admin/users/{userCreatedId}/status
    @PutMapping("/{userCreatedId}/status")
    @Operation(summary = "회원 상태 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 상태 변경 완료"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상태")
    })
    public ResponseEntity<Void> modifyUserStatus(@RequestHeader("X-User-Id") Long adminId,
                                                 @PathVariable Long userCreatedId,
                                                 @RequestBody AccountStatusRequest request) {
        adminService.modifyAccountStatus(adminId, userCreatedId, request);

        return ResponseEntity.ok().build();
    }

    // PUT /api/admin/users/{userCreatedId}/grade
    @PutMapping("/{userCreatedId}/grade")
    @Operation(summary = "회원 등급 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 등급 변경 완료"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 등급")
    })
    public ResponseEntity<Void> modifyUserGrade(@RequestHeader("X-User-Id") Long adminId,
                                                @PathVariable Long userCreatedId,
                                                @RequestBody UserGradeRequest request) {
        adminService.modifyUserGrade(adminId, userCreatedId, request);

        return ResponseEntity.ok().build();
    }

}
