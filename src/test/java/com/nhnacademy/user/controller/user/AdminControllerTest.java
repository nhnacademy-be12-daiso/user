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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.dto.request.AccountStatusRequest;
import com.nhnacademy.user.dto.request.UserGradeRequest;
import com.nhnacademy.user.dto.response.UserDetailResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.service.user.AdminService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @Test
    @DisplayName("전체 회원 목록 조회")
    void test1() throws Exception {
        UserResponse response = new UserResponse(
                1L, "testUser", "홍길동", "010-1234-5678", "test@test.com", null,
                "GENERAL", BigDecimal.ZERO, "ACTIVE", LocalDateTime.now());

        Page<UserResponse> page = new PageImpl<>(List.of(response));
        given(adminService.getAllUsers(any(), any())).willReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].loginId").value("testUser"))
                .andDo(print());
    }

    @Test
    @DisplayName("특정 회원 상세 조회")
    void test2() throws Exception {
        Long userId = 1L;
        UserDetailResponse response = new UserDetailResponse(
                userId, "detailUser", "상세유저", "010-1111-2222", "detail@test.com", null,
                "ACTIVE", "VIP", "USER", BigDecimal.valueOf(5000), LocalDateTime.now(), LocalDateTime.now()
        );

        given(adminService.getUserDetail(userId)).willReturn(response);

        mockMvc.perform(get("/api/admin/users/{userCreatedId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("detailUser"))
                .andExpect(jsonPath("$.gradeName").value("VIP"))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 상태 변경 (BANNED)")
    void test3() throws Exception {
        Long adminId = 999L;
        Long targetId = 1L;
        AccountStatusRequest request = new AccountStatusRequest("BANNED");

        doNothing().when(adminService).modifyUserStatus(eq(adminId), eq(targetId), any());

        mockMvc.perform(put("/api/admin/users/{userCreatedId}/status", targetId)
                        .header("X-User-Id", adminId) // 헤더 필수
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 등급 변경 (PLATINUM)")
    void test4() throws Exception {
        Long adminId = 999L;
        Long targetId = 1L;
        UserGradeRequest request = new UserGradeRequest("PLATINUM");

        doNothing().when(adminService).modifyUserGrade(eq(adminId), eq(targetId), any());

        mockMvc.perform(put("/api/admin/users/{userCreatedId}/grade", targetId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

}
