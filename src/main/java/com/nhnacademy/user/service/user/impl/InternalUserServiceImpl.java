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

package com.nhnacademy.user.service.user.impl;

import com.nhnacademy.user.dto.response.InternalAddressResponse;
import com.nhnacademy.user.dto.response.InternalUserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.address.AddressRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.user.InternalUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 다른 API에 넘겨줄 회원 정보
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class InternalUserServiceImpl implements InternalUserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    private static final String WITHDRAWN_STATUS = "WITHDRAWN";

    /**
     * 회원 유효성을 검증하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @return 회원 존재 여부
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsUser(Long userCreatedId) {
        return userRepository.existsById(userCreatedId);
    }

    /**
     * (주문/결제용) 회원 정보를 조회하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @return Users 테이블 PK, 회원 정보 (이름, 연락처, 이메일, 등급 이름, 적립률, 보유 포인트, 배송지 목록)
     */
    @Override
    @Transactional(readOnly = true)
    public InternalUserResponse getInternalUserInfo(Long userCreatedId) {
        User user = userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> {
                    log.warn("[InternalUserService] 회원 정보 조회 실페: 찾을 수 없는 회원 ({})", userCreatedId);
                    return new UserNotFoundException("찾을 수 없는 회원입니다.");
                });

        Account account = user.getAccount();

        if (WITHDRAWN_STATUS.equals(account.getStatus().getStatusName())) {
            log.warn("[InternalUserService] 회원 정보 조회 실패: 탈퇴한 계정");
            throw new UserNotFoundException("이미 탈퇴한 계정입니다.");
        }

        List<InternalAddressResponse> addresses = addressRepository.findAllByUser(user).stream()
                .map(address -> new InternalAddressResponse(
                        address.getAddressId(),
                        address.getAddressName(),
                        address.getZipCode(),
                        address.getRoadAddress(),
                        address.getAddressDetail(),
                        address.isDefault()))
                .toList();

        return new InternalUserResponse(
                userCreatedId,
                user.getUserName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getGrade().getGradeName(),
                user.getGrade().getPointRate(),
                user.getCurrentPoint(),
                addresses);
    }

}
