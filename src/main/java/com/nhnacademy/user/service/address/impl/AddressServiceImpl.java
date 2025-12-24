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
import com.nhnacademy.user.exception.address.DefaultAddressRequiredException;
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

    /**
     * 새 배송지를 추가하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @param request       배송지 별칭, 우편 번호, 도로명 주소, 상세 주소
     * @return Addresses 테이블에 추가된 PK
     */
    @Override
    @Transactional
    public Long addAddress(Long userCreatedId, AddressRequest request) {
        User user = getUser(userCreatedId);

        long addressCount = addressRepository.countByUser(user);

        // 주소 개수 확인
        if (addressCount >= 10) {
            log.warn("[AddressService] 새 배송지 추가 실패: 주소 최대 개수 초과");
            throw new AddressLimitExceededException("최대 10개의 주소만 등록할 수 있습니다.");
        }

        // 사용자가 기본 배송지로 설정했거나 처음으로 주소를 등록할 때 해당 주소를 기본 배송지로 설정
        boolean isDefault = request.isDefault() || (addressCount == 0);

        // 요청이 기본 배송지로 왔을 때 기존 기본 배송지를 초기화
        if (isDefault) {
            addressRepository.clearAllDefaultsByUser(user);
        }

        Address address = new Address(user,
                request.addressName(), request.zipCode(), request.roadAddress(), request.addressDetail(), isDefault);

        return addressRepository.save(address).getAddressId();
    }

    /**
     * 회원이 등록한 배송지 목록을 조회하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @return 회원이 등록한 배송지 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(Long userCreatedId) {
        User user = getUser(userCreatedId);

        return addressRepository.findAllByUser(user).stream()
                .map(address ->
                        new AddressResponse(address.getAddressId(),
                                address.getAddressName(),
                                address.getZipCode(),
                                address.getRoadAddress(),
                                address.getAddressDetail(),
                                address.isDefault()))
                .collect(Collectors.toList());
    }

    /**
     * 이미 등록된 배송지의 정보를 수정하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @param addressId     Addresses 테이블 PK
     * @param request       배송지 별칭, 우편 번호, 도로명 주소, 상세 주소
     */
    @Override
    @Transactional
    public void modifyAddress(Long userCreatedId, Long addressId, AddressRequest request) {
        User user = getUser(userCreatedId);

        // 요청이 기본 배송지로 왔을 때 기존 기본 배송지를 초기화
        if (request.isDefault()) {
            addressRepository.clearAllDefaultsByUser(user);
        }

        Address address = getAddress(addressId, user);

        // 사용자가 직접 기본 배송지 설정 해제를 하지 않고 다른 주소를 기본 배송지로 설정했을 때 자동으로 해제
        if (address.isDefault() && !request.isDefault()) {
            log.warn("[AddressService] 배송지 수정 실패: 기본 배송지 해제");
            throw new DefaultAddressRequiredException("기본 배송지 설정은 해제할 수 없습니다. 다른 배송지를 기본 배송지로 설정하면 자동으로 변경됩니다.");
        }

        address.modifyDetails(request.addressName(),
                request.zipCode(), request.roadAddress(), request.addressDetail(), request.isDefault());
    }

    /**
     * 이미 등록된 배송지를 삭제하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @param addressId     Addresses 테이블 PK
     */
    @Override
    @Transactional
    public void deleteAddress(Long userCreatedId, Long addressId) {
        User user = getUser(userCreatedId);

        Address address = getAddress(addressId, user);

        // 지우려는 배송지가 기본 배송지면 예외
        if (address.isDefault()) {
            log.warn("[AddressService] 배송지 삭제 실패: 기본 배송지 삭제");
            throw new DefaultAddressDeletionException("기본 배송지는 삭제할 수 없습니다.");
        }

        addressRepository.delete(address);
    }

    private User getUser(Long userCreatedId) {
        return userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> {
                    log.warn("[AddressService] 찾을 수 없는 회원 ({})", userCreatedId);
                    return new UserNotFoundException("찾을 수 없는 회원입니다.");
                });
    }

    private Address getAddress(Long addressId, User user) {
        return addressRepository.findByAddressIdAndUser(addressId, user)
                .orElseThrow(() -> {
                    log.warn("[AddressService] 찾을 수 없는 배송지 ({})", addressId);
                    return new AddressNotFoundException("찾을 수 없는 배송지입니다.");
                });
    }

}
