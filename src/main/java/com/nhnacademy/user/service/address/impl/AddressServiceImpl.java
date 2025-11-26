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
import com.nhnacademy.user.exception.address.AddressLimitExceededException;
import com.nhnacademy.user.exception.address.AddressNotFoundException;
import com.nhnacademy.user.exception.address.DefaultAddressDeletionException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.address.AddressRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.address.AddressService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    private final UserRepository userRepository;

    private final AddressRepository addressRepository;

    @Override
    @Transactional
    public void addAddress(Long userCreatedId, AddressRequest request) {    // 새 배송지 추가
        User user = getUser(userCreatedId);

        // 주소 개수 확인
        if (addressRepository.countByUser(user) >= 10) {
            throw new AddressLimitExceededException("최대 10개의 주소만 등록할 수 있습니다.");
        }

        // 요청이 기본 배송지로 왔을 때 기존 기본 배송지를 초기화
        if (request.isDefault()) {
            addressRepository.clearAllDefaultsByUser(user);
        }

        Address address = new Address(
                user, request.addressName(), request.roadAddress(), request.addressDetail(), request.isDefault());

        Address saved = addressRepository.save(address);

        log.info("배송지 추가 - userCreatedId: {}, addressId: {}", userCreatedId, saved.getAddressId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(Long userCreatedId) {   // 모든 주소 목록 조회
        User user = getUser(userCreatedId);

        List<Address> addresses = addressRepository.findAllByUser(user);

        return addresses.stream()
                .map(address -> new AddressResponse(address.getAddressId(), address.getAddressName(),
                        address.getRoadAddress(), address.getAddressDetail(), address.isDefault()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void modifyAddress(Long userCreatedId, Long addressId, AddressRequest request) { // 특정 주소 정보 수정
        User user = getUser(userCreatedId);

        // 요청이 기본 배송지로 왔을 때 기존 기본 배송지를 초기화
        if (request.isDefault()) {
            addressRepository.clearAllDefaultsByUser(user);
        }

        Address address = getAddress(addressId, user);

        address.modifyDetails(
                request.addressName(), request.roadAddress(), request.addressDetail(), request.isDefault());
    }

    @Override
    @Transactional
    public void deleteAddress(Long userCreatedId, Long addressId) { // 특정 주소 삭제
        User user = getUser(userCreatedId);

        Address address = getAddress(addressId, user);

        // 지우려는 배송지가 기본 배송지면 예외
        if (address.isDefault()) {
            throw new DefaultAddressDeletionException("기본 배송지는 삭제할 수 없습니다.");
        }

        addressRepository.delete(address);

        log.info("배송지 삭제 - userCreatedId: {}, addressId: {}", userCreatedId, addressId);
    }

    private User getUser(Long userCreatedId) {
        return userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));
    }

    private Address getAddress(Long addressId, User user) {
        return addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> new AddressNotFoundException("찾을 수 없는 주소입니다."));
    }

}
