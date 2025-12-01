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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhnacademy.user.service.account.AccountService;
import com.nhnacademy.user.service.message.VerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificationService verificationService;

    @MockitoBean
    private AccountService accountService;

    @Test
    @DisplayName("휴면 해제 인증코드 발송")
    void test6() throws Exception {
        Long userId = 1L;
        doNothing().when(verificationService).sendCode(userId);

        mockMvc.perform(post("/api/users/activate/send-code")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("휴면 계정 활성화")
    void test7() throws Exception {
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

}
