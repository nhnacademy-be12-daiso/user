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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.service.message.VerificationService;
import com.nhnacademy.user.service.user.UserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@TestPropertySource(properties = {
        "dooray.hook.url=https://nhnacademy.dooray.com/services/3204376758577275363/4208785448943273505/P-UcX5QCSY-AxUjp0zz_Xw",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserControllerXUserIdTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private VerificationService verificationService;

    @Test
    @DisplayName("X-User-Id 헤더로 내 정보 조회 성공")
    void getMyInfo_WithXUserIdHeader_Success() throws Exception {
        // given
        Long userCreatedId = 29L;
        UserResponse response = new UserResponse(
                1L,
                "testuser",
                "테스트유저",
                "01012345678",
                "test@example.com",
                LocalDate.of(1990, 1, 1),
                "GENERAL",
                1000L,
                "ACTIVE",
                LocalDateTime.now()
        );

        given(userService.getUserInfo(userCreatedId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", "29")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("testuser"))
                .andExpect(jsonPath("$.userName").value("테스트유저"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("X-User-Id 헤더 없음 - 400 Bad Request")
    void getMyInfo_WithoutXUserIdHeader_Returns400() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("X-User-Id 헤더가 비어있음 - 400 Bad Request")
    void getMyInfo_WithEmptyXUserIdHeader_Returns400() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("X-User-Id 헤더 값이 숫자가 아님 - 500 Internal Server Error")
    void getMyInfo_WithInvalidXUserIdHeader_Returns400() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("X-User-Id로 다른 사용자 조회")
    void getMyInfo_DifferentUserId() throws Exception {
        // given
        Long userId1 = 10L;
        Long userId2 = 20L;

        UserResponse response1 = new UserResponse(
                1L, "user1", "사용자1", "01011111111", "user1@test.com",
                LocalDate.of(1990, 1, 1), "GENERAL", 0L, "ACTIVE", LocalDateTime.now()
        );

        UserResponse response2 = new UserResponse(
                1L, "user2", "사용자2", "01022222222", "user2@test.com",
                LocalDate.of(1991, 2, 2), "VIP", 5000L, "ACTIVE", LocalDateTime.now()
        );

        given(userService.getUserInfo(userId1)).willReturn(response1);
        given(userService.getUserInfo(userId2)).willReturn(response2);

        // when & then - 첫 번째 사용자
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("user1"))
                .andExpect(jsonPath("$.userName").value("사용자1"));

        // when & then - 두 번째 사용자
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("user2"))
                .andExpect(jsonPath("$.userName").value("사용자2"));
    }

    @Test
    @DisplayName("X-User-Id가 Long 타입으로 정상 파싱됨")
    void xUserIdIsParsedAsLong() throws Exception {
        // given
        Long expectedUserId = 99999L;
        UserResponse response = new UserResponse(
                1L, "testuser", "테스트", "01012345678", "test@test.com",
                LocalDate.of(1990, 1, 1), "GENERAL", 0L, "ACTIVE", LocalDateTime.now()
        );

        given(userService.getUserInfo(expectedUserId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", String.valueOf(expectedUserId)))
                .andExpect(status().isOk());
    }
}

