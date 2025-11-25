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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

@Tag(name = "주소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/addresses")
public class AddressController {

    private final AddressService addressService;

    // POST /api/users/me/addresses
    @Operation(summary = "새 배송지 추가")
    @PostMapping
    public ResponseEntity<Void> addMyAddress(@RequestHeader(name = "X-USER-ID") Long userCreatedId,
                                             @Valid @RequestBody AddressRequest request) {
        addressService.addAddress(userCreatedId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/users/me/addresses
    @Operation(summary = "내 주소 목록 조회")
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses(@RequestHeader(name = "X-USER-ID") Long userCreatedId) {
        List<AddressResponse> addresses = addressService.getMyAddresses(userCreatedId);

        return ResponseEntity.status(HttpStatus.OK).body(addresses);
    }

    // PUT /api/users/me/addresses/{addressId}
    @Operation(summary = "주소 수정")
    @PutMapping("/{addressId}")
    public ResponseEntity<Void> modifyAddress(@RequestHeader(name = "X-USER-ID") Long userCreatedId,
                                              @PathVariable Long addressId,
                                              @Valid @RequestBody AddressRequest request) {
        addressService.modifyAddress(userCreatedId, addressId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // DELETE /api/users/me/addresses/{addressId}
    @Operation(summary = "주소 삭제")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@RequestHeader(name = "X-USER-ID") Long userCreatedId,
                                              @PathVariable Long addressId) {
        addressService.deleteAddress(userCreatedId, addressId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
