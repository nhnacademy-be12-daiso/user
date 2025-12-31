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

package com.nhnacademy.user.controller.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.dto.request.FindLoginIdRequest;
import com.nhnacademy.user.dto.request.FindPasswordRequest;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.service.account.AccountService;
import com.nhnacademy.user.service.account.FindAccountService;
import com.nhnacademy.user.service.message.VerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private VerificationService verificationService;

    @MockitoBean
    private FindAccountService findAccountService;

    @Test
    @DisplayName("휴면 해제 인증코드 발송")
    void test1() throws Exception {
        Long userId = 1L;

        doNothing().when(verificationService).sendCode(userId);

        mockMvc.perform(post("/api/users/activate/send-code")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("휴면 해제 인증코드 발송 실패 - 존재하지 않는 유저 (404)")
    void test2() throws Exception {
        Long userId = 999L;
        doThrow(new UserNotFoundException("User not found"))
                .when(verificationService).sendCode(userId);

        mockMvc.perform(post("/api/users/activate/send-code")
                        .header("X-User-Id", userId))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("휴면 계정 활성화 (인증코드 필수)")
    void test3() throws Exception {
        Long userId = 1L;
        String code = "123456";

        doNothing().when(verificationService).verifyCode(anyLong(), anyString());
        doNothing().when(accountService).activeUser(anyLong());

        mockMvc.perform(post("/api/users/activate")
                        .header("X-User-Id", userId)
                        .param("code", code))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("휴면 계정 활성화 실패 - 인증코드 불일치 (401)")
    void test4() throws Exception {
        Long userId = 1L;
        String wrongCode = "000000";

        doThrow(new InvalidCodeException("Invalid code"))
                .when(verificationService).verifyCode(userId, wrongCode);

        mockMvc.perform(post("/api/users/activate")
                        .header("X-User-Id", userId)
                        .param("code", wrongCode))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("아이디 찾기")
    void test5() throws Exception {
        FindLoginIdRequest request = new FindLoginIdRequest("홍길동", "test@test.com");
        String maskedId = "test***";

        given(findAccountService.findLoginId(any(FindLoginIdRequest.class))).willReturn(maskedId);

        mockMvc.perform(post("/api/users/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(maskedId))
                .andDo(print());
    }

    @Test
    @DisplayName("아이디 찾기 실패 - 회원 정보 없음 (404)")
    void test6() throws Exception {
        FindLoginIdRequest request = new FindLoginIdRequest("없는사람", "no@test.com");

        given(findAccountService.findLoginId(any(FindLoginIdRequest.class)))
                .willThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/users/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 찾기 (임시 비밀번호 발급)")
    void test7() throws Exception {
        FindPasswordRequest request = new FindPasswordRequest("testId", "홍길동", "test@test.com");

        doNothing().when(findAccountService).createTemporaryPassword(any(FindPasswordRequest.class));

        mockMvc.perform(post("/api/users/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("아이디 중복 확인")
    void test8() throws Exception {
        String loginId = "test1";

        mockMvc.perform(get("/api/users/check-id")
                        .param("id", loginId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andDo(print());
    }

}
