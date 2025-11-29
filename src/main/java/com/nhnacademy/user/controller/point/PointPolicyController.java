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

package com.nhnacademy.user.controller.point;

import com.nhnacademy.user.dto.request.PointPolicyRequest;
import com.nhnacademy.user.dto.response.PointPolicyResponse;
import com.nhnacademy.user.service.point.PointPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "포인트 정책 API - 관리자 전용")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/points/policies")
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;

    // POST /api/admin/points/policies
    @Operation(summary = "포인트 정책 등록")
    @PostMapping
    public ResponseEntity<Void> createPolicy(@RequestHeader("X-User-Id") Long userCreatedId,
                                             @Valid @RequestBody PointPolicyRequest request) {
        pointPolicyService.createPolicy(request);

        log.info("관리자 [{}] - 포인트 정책 등록", userCreatedId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/admin/points/policies
    @Operation(summary = "포인트 정책 전체 조회")
    @GetMapping
    public ResponseEntity<List<PointPolicyResponse>> getPolicies() {
        return ResponseEntity.status(HttpStatus.OK).body(pointPolicyService.getPolicies());
    }

    // PUT /api/admin/points/policies
    @Operation(summary = "포인트 정책 수정")
    @PutMapping("/{pointPolicyId}")
    public ResponseEntity<Void> modifyPolicy(@RequestHeader("X-User-Id") Long userCreatedId,
                                             @PathVariable Long pointPolicyId,
                                             @Valid @RequestBody PointPolicyRequest request) {
        pointPolicyService.modifyPolicy(pointPolicyId, request);

        log.info("관리자 [{}] - 포인트 정책 수정", userCreatedId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // DELETE /api/admin/points/policies
    @Operation(summary = "포인트 정책 삭제")
    @DeleteMapping("/{pointPolicyId}")
    public ResponseEntity<Void> deletePolicy(@RequestHeader("X-User-Id") Long userCreatedId,
                                             @PathVariable Long pointPolicyId) {
        pointPolicyService.deletePolicy(pointPolicyId);

        log.info("관리자 [{}] - 포인트 정책 삭제", userCreatedId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
