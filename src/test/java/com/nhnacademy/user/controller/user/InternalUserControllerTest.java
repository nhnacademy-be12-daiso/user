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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhnacademy.user.dto.response.InternalUserResponse;
import com.nhnacademy.user.service.user.InternalUserService;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalUserController.class)
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InternalUserService internalUserService;

    @Test
    @DisplayName("회원 존재 여부 확인")
    void test1() throws Exception {
        Long userId = 1L;
        given(internalUserService.existsUser(userId)).willReturn(true);

        mockMvc.perform(get("/api/internal/users/{userCreatedId}/exists", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("내부용 회원 정보 조회")
    void test2() throws Exception {
        Long userId = 1L;
        InternalUserResponse response = new InternalUserResponse(
                userId, "name", "phone", "email", "GOLD", BigDecimal.valueOf(2.50), null, null);
        given(internalUserService.getInternalUserInfo(userId)).willReturn(response);

        mockMvc.perform(get("/api/internal/users/{userCreatedId}/info", userId))
                .andExpect(status().isOk());
    }

}
