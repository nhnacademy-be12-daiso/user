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

import com.nhnacademy.user.dto.request.AddressRequest;
import com.nhnacademy.user.dto.response.AddressResponse;
import com.nhnacademy.user.dto.response.ErrorResponse;
import com.nhnacademy.user.service.address.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "주소 API", description = "새 배송지 추가, 주소 목록 조회, 주소 수정, 주소 삭제  API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class AddressController {

    private final AddressService addressService;

    // POST /users/me/addresses
    @Operation(summary = "새 배송지 추가", description = "로그인한 사용자의 새로운 배송지를 등록합니다. (최대 10개)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "주소 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음)", content = @Content(schema = @Schema(hidden = true)))})
    @PostMapping("/addresses")
    public ResponseEntity<Void> addMyAddress(Authentication authentication,
                                             @Valid @RequestBody AddressRequest request) {
        // Authentication 객체에서 로그인 아이디(principal) 가져옴
        String loginId = (String) authentication.getPrincipal();

        addressService.addAddress(loginId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /users/me/addresses
    @Operation(summary = "내 주소 목록 조회", description = "로그인한 사용자의 모든 배송지 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AddressResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음)", content = @Content(schema = @Schema(hidden = true)))})
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> getMyAddresses(Authentication authentication) {
        // Authentication 객체에서 로그인 아이디(principal) 가져옴
        String loginId = (String) authentication.getPrincipal();

        List<AddressResponse> addresses = addressService.getMyAddresses(loginId);

        return ResponseEntity.status(HttpStatus.OK).body(addresses);
    }

    // PUT /users/me/addresses/{addressId}
    @Operation(summary = "주소 수정", description = "특정 배송지의 정보(별칭, 상세주소, 기본배송지 여부)를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주소 (또는 본인 주소 아님)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<Void> updateAddress(Authentication authentication,
                                              @PathVariable Long addressId,
                                              @Valid @RequestBody AddressRequest request) {
        // Authentication 객체에서 로그인 아이디(principal) 가져옴
        String loginId = (String) authentication.getPrincipal();

        addressService.updateAddress(loginId, addressId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // DELETE /users/me/addresses/{addressId}
    @Operation(summary = "주소 삭제", description = "특정 배송지를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주소 (또는 본인 주소 아님)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(Authentication authentication,
                                              @PathVariable Long addressId) {
        // Authentication 객체에서 로그인 아이디(principal) 가져옴
        String loginId = (String) authentication.getPrincipal();

        addressService.deleteAddress(loginId, addressId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
