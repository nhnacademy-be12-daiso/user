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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.service.user.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void test1() throws Exception {
        SignupRequest request = new SignupRequest(
                "test", "password123!", "홍길동",
                "010-1234-5678", "test@email.com", LocalDate.now().minusDays(1));

        doNothing().when(userService).signUp(any());

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void test2() throws Exception {
        Long userId = 1L;
        UserResponse response = new UserResponse(
                1L, "testId", "홍길동", "010-1234-5678", "test@email.com", LocalDate.now(),
                "GENERAL", BigDecimal.ZERO, "ACTIVE", LocalDateTime.now());

        given(userService.getUserInfo(userId)).willReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("testId"))
                .andExpect(jsonPath("$.gradeName").value("GENERAL"))
                .andDo(print());
    }

    @Test
    @DisplayName("내 정보 수정 성공")
    void test3() throws Exception {
        Long userId = 1L;
        UserModifyRequest request = new UserModifyRequest(
                "개명", "010-9999-9999", "new@test.com", LocalDate.now().minusDays(1));

        doNothing().when(userService).modifyUserInfo(eq(userId), any());

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    void test4() throws Exception {
        Long userId = 1L;
        PasswordModifyRequest request = new PasswordModifyRequest(
                "oldPass123!@#", "newPass123!@#");

        doNothing().when(userService).modifyUserPassword(eq(userId), any());

        mockMvc.perform(put("/api/users/me/password")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void test5() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).withdrawUser(userId);

        mockMvc.perform(delete("/api/users/me")
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

}
