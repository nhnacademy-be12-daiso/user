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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.dto.request.PointPolicyRequest;
import com.nhnacademy.user.dto.response.PointPolicyResponse;
import com.nhnacademy.user.entity.point.Method;
import com.nhnacademy.user.service.point.PointPolicyService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointPolicyController.class)
class PointPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PointPolicyService pointPolicyService;

    @Test
    @DisplayName("포인트 정책 등록")
    void test1() throws Exception {
        PointPolicyRequest request = new PointPolicyRequest(
                "정책", "TYPE", Method.AMOUNT, BigDecimal.valueOf(100));

        doNothing().when(pointPolicyService).createPolicy(any());

        mockMvc.perform(post("/api/admin/points/policies")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("포인트 정책 전체 조회")
    void test2() throws Exception {
        List<PointPolicyResponse> list = List.of(
                new PointPolicyResponse(
                        1L, "정책", "TYPE", Method.AMOUNT, BigDecimal.valueOf(100)));

        given(pointPolicyService.getPolicies()).willReturn(list);

        mockMvc.perform(get("/api/admin/points/policies"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("포인트 정책 수정")
    void test3() throws Exception {
        Long policyId = 1L;
        PointPolicyRequest request = new PointPolicyRequest(
                "수정", "TYPE", Method.AMOUNT, BigDecimal.valueOf(200));

        doNothing().when(pointPolicyService).modifyPolicy(eq(policyId), any());

        mockMvc.perform(put("/api/admin/points/policies/{pointPolicyId}", policyId)
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("포인트 정책 삭제")
    void test4() throws Exception {
        Long policyId = 1L;
        doNothing().when(pointPolicyService).deletePolicy(policyId);

        mockMvc.perform(delete("/api/admin/points/policies/{pointPolicyId}", policyId)
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());
    }

}
