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

package com.nhnacademy.user.controller.address;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.dto.request.AddressRequest;
import com.nhnacademy.user.dto.response.AddressResponse;
import com.nhnacademy.user.service.address.AddressService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

    @Test
    @DisplayName("배송지 추가 성공")
    void test1() throws Exception {
        Long userId = 1L;
        AddressRequest request = new AddressRequest(
                "집", "12345", "광주", "101호", true);

        doNothing().when(addressService).addAddress(any(), any());

        mockMvc.perform(post("/api/users/me/addresses")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("배송지 목록 조회")
    void test2() throws Exception {
        Long userId = 1L;
        List<AddressResponse> list = List.of(new AddressResponse(
                1L, "집", "12345", "광주", "101호", true));

        given(addressService.getMyAddresses(userId)).willReturn(list);

        mockMvc.perform(get("/api/users/me/addresses")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addressName").value("집"));
    }

    @Test
    @DisplayName("배송지 수정")
    void test3() throws Exception {
        Long userId = 1L;
        Long addressId = 10L;
        AddressRequest request = new AddressRequest(
                "회사", "12345", "서울", "빌딩", false);

        doNothing().when(addressService).modifyAddress(eq(userId), eq(addressId), any());

        mockMvc.perform(put("/api/users/me/addresses/{addressId}", addressId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("배송지 삭제")
    void test4() throws Exception {
        Long userId = 1L;
        Long addressId = 10L;

        doNothing().when(addressService).deleteAddress(userId, addressId);

        mockMvc.perform(delete("/api/users/me/addresses/{addressId}", addressId)
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent());
    }

}
