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
import com.nhnacademy.user.service.address.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "배송지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/addresses")
public class AddressController {

    private final AddressService addressService;

    // POST /api/users/me/addresses
    @PostMapping
    @Operation(summary = "새 배송지 추가")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "새 배송지 추가 완료"),
            @ApiResponse(responseCode = "400", description = "등록된 주소 10개 초과"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    })
    public ResponseEntity<Void> addMyAddress(@RequestHeader("X-User-Id") Long userCreatedId,
                                             @Valid @RequestBody AddressRequest request) {
        Long addressId = addressService.addAddress(userCreatedId, request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(addressId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // GET /api/users/me/addresses
    @GetMapping
    @Operation(summary = "내 배송지 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 배송지 목록 조회 완료"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    })
    public ResponseEntity<List<AddressResponse>> getMyAddresses(@RequestHeader("X-User-Id") Long userCreatedId) {
        return ResponseEntity.ok().body(addressService.getMyAddresses(userCreatedId));
    }

    // PUT /api/users/me/addresses/{addressId}
    @PutMapping("/{addressId}")
    @Operation(summary = "배송지 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배송지 수정 완료"),
            @ApiResponse(responseCode = "400", description = "기본 배송지 설정 해제"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주소")
    })
    public ResponseEntity<Void> modifyAddress(@RequestHeader("X-User-Id") Long userCreatedId,
                                              @PathVariable Long addressId,
                                              @Valid @RequestBody AddressRequest request) {
        addressService.modifyAddress(userCreatedId, addressId, request);

        return ResponseEntity.ok().build();
    }

    // DELETE /api/users/me/addresses/{addressId}
    @DeleteMapping("/{addressId}")
    @Operation(summary = "배송지 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "배송지 삭제 완료"),
            @ApiResponse(responseCode = "400", description = "기본 배송지 삭제"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주소")
    })
    public ResponseEntity<Void> deleteAddress(@RequestHeader("X-User-Id") Long userCreatedId,
                                              @PathVariable Long addressId) {
        addressService.deleteAddress(userCreatedId, addressId);

        return ResponseEntity.noContent().build();
    }

}
