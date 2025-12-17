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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "포인트 정책 API - 관리자 전용")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/points/policies")
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;

    // POST /api/admin/points/policies
    @PostMapping
    @Operation(summary = "포인트 정책 등록")
    @ApiResponse(responseCode = "409", description = "이미 존재하는 포인트 정책")
    public ResponseEntity<Void> createPolicy(@RequestHeader("X-User-Id") Long userCreatedId,
                                             @Valid @RequestBody PointPolicyRequest request) {
        pointPolicyService.createPolicy(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userCreatedId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // GET /api/admin/points/policies
    @GetMapping
    @Operation(summary = "포인트 정책 전체 조회")
    public ResponseEntity<List<PointPolicyResponse>> getPolicies() {
        return ResponseEntity.ok().body(pointPolicyService.getPolicies());
    }

    // PUT /api/admin/points/policies/{pointPolicyId}
    @PutMapping("/{pointPolicyId}")
    @Operation(summary = "포인트 정책 수정")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 포인트 정책")
    public ResponseEntity<Void> modifyPolicy(@RequestHeader("X-User-Id") Long userCreatedId,
                                             @PathVariable Long pointPolicyId,
                                             @Valid @RequestBody PointPolicyRequest request) {
        pointPolicyService.modifyPolicy(pointPolicyId, request);

        return ResponseEntity.ok().build();
    }

    // DELETE /api/admin/points/policies/{pointPolicyId}
    @DeleteMapping("/{pointPolicyId}")
    @Operation(summary = "포인트 정책 삭제")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 포인트 정책")
    public ResponseEntity<Void> deletePolicy(@RequestHeader("X-User-Id") Long userCreatedId,
                                             @PathVariable Long pointPolicyId) {
        pointPolicyService.deletePolicy(pointPolicyId);

        return ResponseEntity.noContent().build();
    }

}
