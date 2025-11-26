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

import com.nhnacademy.user.dto.response.PointHistoryResponse;
import com.nhnacademy.user.service.point.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "포인트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/points")
public class PointController {

    private final PointService pointService;

    // GET /api/users/me/points
    @Operation(summary = "내 포인트 내역 조회")
    @GetMapping
    public ResponseEntity<Page<PointHistoryResponse>> getMyPoints(@RequestHeader(name = "X-User-Id") Long userCreatedId,
                                                                  @PageableDefault Pageable pageable) {
        Page<PointHistoryResponse> pointHistoryResponses = pointService.getMyPointHistory(userCreatedId, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(pointHistoryResponses);
    }

}
