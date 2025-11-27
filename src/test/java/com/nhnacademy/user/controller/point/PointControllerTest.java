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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nhnacademy.user.dto.response.PointHistoryResponse;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.service.point.PointService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PointService pointService;

    @Test
    @DisplayName("내 포인트 내역 조회")
    void test1() throws Exception {
        Long userId = 1L;
        Page<PointHistoryResponse> page = new PageImpl<>(List.of(
                new PointHistoryResponse(BigDecimal.TEN, Type.EARN, "적립", LocalDateTime.now())
        ));

        given(pointService.getMyPointHistory(eq(userId), any())).willReturn(page);

        mockMvc.perform(get("/api/users/me/points")
                        .header("X-User-Id", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

}
