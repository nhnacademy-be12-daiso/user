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

import com.nhnacademy.user.dto.response.InternalUserResponse;
import com.nhnacademy.user.service.user.InternalUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "내부용 회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private final InternalUserService internalUserService;

    // GET /api/internal/users/{userCreatedId}/exists
    @Operation(summary = "[내부] 회원 유효성 검증")
    @GetMapping("/{userCreatedId}/exists")
    public ResponseEntity<Boolean> existsUser(@PathVariable Long userCreatedId) {
        boolean exists = internalUserService.existsUser(userCreatedId);

        return ResponseEntity.status(HttpStatus.OK).body(exists);
    }

    // GET /api/internal/users/{userCreatedId}/info
    @Operation(summary = "[내부] 주문/결제용 회원 정보 조회")
    @GetMapping("/{userCreatedId}/info")
    public ResponseEntity<InternalUserResponse> getUserInfoForOrder(@PathVariable Long userCreatedId) {
        InternalUserResponse response = internalUserService.getInternalUserInfo(userCreatedId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
