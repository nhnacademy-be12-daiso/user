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

import com.nhnacademy.user.dto.response.BirthdayUserDto;
import com.nhnacademy.user.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserBirthdayController {

    private UserService userService;

    public UserBirthdayController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "생일 월로 사용자 조회", description = "특정 월이 생일인 사용자 목록 조회")
    @GetMapping("/birthday")
    public ResponseEntity<List<BirthdayUserDto>> getBirthdayUsers(
            @RequestParam int month) {
        // month에 해당하는 생일자 조회
        List<BirthdayUserDto> users = userService.findByBirthdayMonth(month);
        return ResponseEntity.ok(users);
    }
}
