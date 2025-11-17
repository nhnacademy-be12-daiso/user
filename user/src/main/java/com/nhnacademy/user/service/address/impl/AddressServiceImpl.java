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

package com.nhnacademy.user.service.address.impl;

import com.nhnacademy.user.dto.request.AddressRequest;
import com.nhnacademy.user.dto.response.AddressResponse;
import com.nhnacademy.user.entity.address.Address;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.AddressLimitExceededException;
import com.nhnacademy.user.exception.AddressNotFoundException;
import com.nhnacademy.user.exception.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.address.AddressRepository;
import com.nhnacademy.user.service.address.AddressService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AddressServiceImpl implements AddressService {

    private final AccountRepository accountRepository;

    private final AddressRepository addressRepository;

    @Override
    @Transactional
    public void addAddress(String loginId, AddressRequest request) {    // 새 배송지 추가
        User user = getUser(loginId);

        // 주소 개수 확인
        if (addressRepository.countByUser(user) >= 10) {
            throw new AddressLimitExceededException("최대 10개의 주소만 등록할 수 있습니다.");
        }

        // 요청이 기본 배송지로 왔을 때 기존 기본 배송지를 초기화
        if (request.isDefault()) {
            addressRepository.clearAllDefaultsByUser(user);
        }

        Address address = new Address(user, request.addressName(), request.addressDetail(), request.isDefault());

        addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(String loginId) {   // 모든 주소 목록 조회
        User user = getUser(loginId);

        List<Address> addresses = addressRepository.findAllByUser(user);

        return addresses.stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateAddress(String loginId, Long addressId, AddressRequest request) { // 특정 주소 정보 수정
        User user = getUser(loginId);

        Address address = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new AddressNotFoundException("찾을 수 없는 주소입니다."));

        // 요청이 기본 배송지로 왔을 때 기존 기본 배송지를 초기화
        if (request.isDefault()) {
            addressRepository.clearAllDefaultsByUser(user);
        }

        address.updateDetails(request.addressName(), request.addressDetail(), request.isDefault());
    }

    @Override
    @Transactional
    public void deleteAddress(String loginId, Long addressId) { // 특정 주소 삭제
        User user = getUser(loginId);

        Address address = addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new AddressNotFoundException("찾을 수 없는 주소입니다."));

        addressRepository.delete(address);
    }

    private User getUser(String loginId) {
        return accountRepository.findByIdWithUser(loginId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 계정입니다."))
                .getUser();
    }

}
