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

import com.nhnacademy.user.dto.request.PointRequest;
import com.nhnacademy.user.service.point.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "내부용 포인트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/points")
public class InternalPointController {
    // Gateway 혹은 다른 MSA 서비스(Order Service 등)에서 회원의 존재 여부를 확인하거나 주문 시 필요한 정보를 조회할 때 호출됩니다.
    // 일반 사용자는 호출할 수 없도록 Gateway에서 막혀야 합니다.

    private final PointService pointService;

    // POST /api/internal/points
    @PostMapping
    @Operation(summary = "[내부] 포인트 적립/사용 처리")
    public ResponseEntity<Void> processPoint(@Valid @RequestBody PointRequest request) {
        pointService.processPoint(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/policy")
    @Operation(summary = "[내부] 정책 기반 포인트 적립")
    public ResponseEntity<Void> earnPointByPolicy(@RequestHeader("X-User-Id") Long userCreatedId,
                                                  String policyType) {
        pointService.earnPointByPolicy(userCreatedId, policyType);

        return ResponseEntity.ok().build();
    }

}
