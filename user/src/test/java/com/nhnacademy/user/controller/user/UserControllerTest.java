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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.config.SecurityConfig;
import com.nhnacademy.user.dto.request.LoginRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.properties.JwtProperties;
import com.nhnacademy.user.service.user.UserService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtProperties.class})
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @Test
    @DisplayName("회원가입 요청 성공 - 201 Created")
    void test1() throws Exception {
        SignupRequest request = new SignupRequest("test", "pwd123!@#", "테스트",
                "010-1234-5678", "test@test.com", LocalDate.of(2003, 11, 7));

        doNothing().when(userService).signUp(any(SignupRequest.class));

        mockMvc.perform(post("/users/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("필수 값 누락 - 400 Bad Request")
    void test2() throws Exception {
        SignupRequest request = new SignupRequest("test", "", "테스트",
                "010-1234-5678", "test@test.com", LocalDate.of(2003, 11, 7));

        mockMvc.perform(post("/users/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 특수문자 누락 - 400 Bad Request")
    void test3() throws Exception {
        SignupRequest request = new SignupRequest("test", "pwd123", "테스트",
                "010-1234-5678", "test@test.com", LocalDate.of(2003, 11, 7));

        mockMvc.perform(post("/users/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 성공 - 200 OK")
    void test4() throws Exception {
        LoginRequest request = new LoginRequest("test", "pwd123!@#");
        String jsonRequest = objectMapper.writeValueAsString(request);
        String token = "Daiso token";

        given(userService.login(any(LoginRequest.class))).willReturn(token);

        mockMvc.perform(post("/users/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(header().string("Authorization", token));
    }

}
