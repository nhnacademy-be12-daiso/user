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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.dto.request.PointRequest;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.exception.point.PointNotEnoughException;
import com.nhnacademy.user.exception.point.PointPolicyNotFoundException;
import com.nhnacademy.user.service.point.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalPointController.class)
class InternalPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PointService pointService;

    @Test
    @DisplayName("[내부] 포인트 적립/사용 처리")
    void test1() throws Exception {
        PointRequest request = new PointRequest(1L, 10L, Type.EARN, "적립");

        doNothing().when(pointService).processPoint(any());

        mockMvc.perform(post("/api/internal/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[내부] 포인트 적립/사용 실패 - 잔액 부족 (400)")
    void test2() throws Exception {
        PointRequest request = new PointRequest(1L, 10000L, Type.USE, "사용");

        doThrow(new PointNotEnoughException("잔액 부족"))
                .when(pointService).processPoint(any(PointRequest.class));

        mockMvc.perform(post("/api/internal/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[내부] 포인트 적립/사용 실패 - 유효성 검증 실패 (400)")
    void test3() throws Exception {
        PointRequest invalidRequest = new PointRequest(null, 10L, Type.EARN, "적립");

        mockMvc.perform(post("/api/internal/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[내부] 정책 기반 포인트 적립 - 성공")
    void test4() throws Exception {
        Long userId = 1L;
        String policyType = "REGISTER";

        doNothing().when(pointService).earnPointByPolicy(anyLong(), anyString());

        mockMvc.perform(post("/api/internal/points/policy")
                        .header("X-User-Id", userId)
                        .param("policy-type", policyType))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[내부] 정책 기반 포인트 적립 실패 - 존재하지 않는 정책 (404)")
    void test5() throws Exception {
        Long userId = 1L;
        String policyType = "UNKNOWN";

        doThrow(new PointPolicyNotFoundException("정책 없음"))
                .when(pointService).earnPointByPolicy(userId, policyType);

        mockMvc.perform(post("/api/internal/points/policy")
                        .header("X-User-Id", userId)
                        .param("policy-type", policyType))
                .andExpect(status().isNotFound());
    }

}
