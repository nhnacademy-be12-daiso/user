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
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.dto.request.AddressRequest;
import com.nhnacademy.user.dto.response.AddressResponse;
import com.nhnacademy.user.exception.address.AddressLimitExceededException;
import com.nhnacademy.user.exception.address.AddressNotFoundException;
import com.nhnacademy.user.exception.address.DefaultAddressDeletionException;
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
    @DisplayName("새 배송지 추가")
    void test1() throws Exception {
        Long userId = 1L;
        AddressRequest request = new AddressRequest(
                "집", "12345", "광주", "101호", true);

        given(addressService.addAddress(eq(userId), any(AddressRequest.class)))
                .willReturn(100L);

        mockMvc.perform(post("/api/users/me/addresses")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("새 배송지 추가 실패 - 주소 개수 초과 (400)")
    void test2() throws Exception {
        Long userId = 1L;
        AddressRequest request = new AddressRequest("집", "12345", "광주", "상세", false);

        given(addressService.addAddress(eq(userId), any(AddressRequest.class)))
                .willThrow(new AddressLimitExceededException("Limit exceeded"));

        mockMvc.perform(post("/api/users/me/addresses")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 배송지 추가 실패 - 유효성 검증 실패 (400)")
    void test3() throws Exception {
        AddressRequest invalidRequest = new AddressRequest(null, null, null, null, false);

        mockMvc.perform(post("/api/users/me/addresses")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 배송지 목록 조회")
    void test4() throws Exception {
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
    void test5() throws Exception {
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
    @DisplayName("배송지 수정 실패 - 존재하지 않는 주소 (404)")
    void test6() throws Exception {
        Long userId = 1L;
        Long addressId = 999L;
        AddressRequest request = new AddressRequest("회사", "12345", "서울", "빌딩", false);

        doThrow(new AddressNotFoundException("Address not found"))
                .when(addressService).modifyAddress(eq(userId), eq(addressId), any());

        mockMvc.perform(put("/api/users/me/addresses/{addressId}", addressId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("배송지 삭제")
    void test7() throws Exception {
        Long userId = 1L;
        Long addressId = 10L;

        doNothing().when(addressService).deleteAddress(userId, addressId);

        mockMvc.perform(delete("/api/users/me/addresses/{addressId}", addressId)
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 기본 배송지 삭제 시도 (400)")
    void test8() throws Exception {
        Long userId = 1L;
        Long addressId = 1L;

        doThrow(new DefaultAddressDeletionException("Cannot delete default address"))
                .when(addressService).deleteAddress(userId, addressId);

        mockMvc.perform(delete("/api/users/me/addresses/{addressId}", addressId)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

}
