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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "내부용 포인트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/points")
public class InternalPointController {

    private final PointService pointService;

    // POST /api/internal/points
    @Operation(summary = "포인트 적립/사용 처리")
    @PostMapping
    public ResponseEntity<Void> processPoint(@Valid @RequestBody PointRequest request) {

        // 서비스 로직 호출 (User 업데이트 + History 저장)
        pointService.processPoint(request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
