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
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import com.nhnacademy.user.exception.account.StateNotFoundException;
import com.nhnacademy.user.exception.user.GradeNotFoundException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.address.AddressRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.point.PointService;
import com.nhnacademy.user.service.user.InternalUserService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class InternalUserServiceImpl implements InternalUserService {

    private final UserRepository userRepository;

    private final AddressRepository addressRepository;

    private final UserGradeHistoryRepository userGradeHistoryRepository;

    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    private final PointService pointService;

    private static final String WITHDRAWN_STATUS = "WITHDRAWN";

    @Override
    @Transactional(readOnly = true)
    public boolean existsUser(Long userCreatedId) { // 회원 유효성 검증
        return userRepository.existsById(userCreatedId);
    }

    @Override
    @Transactional(readOnly = true)
    public InternalUserResponse getInternalUserInfo(Long userCreatedId) {   // 주문/결제용 회원 정보 조회
        User user = userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));

        Account account = user.getAccount();

        Status status = accountStatusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(account)
                .map(AccountStatusHistory::getStatus)
                .orElseThrow(() -> new StateNotFoundException("존재하지 않는 상태입니다."));

        if (WITHDRAWN_STATUS.equals(status.getStatusName())) {
            throw new UserNotFoundException("탈퇴한 계정입니다.");
        }

        Grade grade = userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(user)
                .map(UserGradeHistory::getGrade)
                .orElseThrow(() -> new GradeNotFoundException("존재하지 않는 등급입니다."));

        BigDecimal point = pointService.getCurrentPoint(userCreatedId).currentPoint();

        List<InternalAddressResponse> addresses = addressRepository.findAllByUser(user).stream()
                .map(address -> new InternalAddressResponse(
                        address.getAddressName(), address.getZipCode(), address.getRoadAddress(),
                        address.getAddressDetail()))
                .toList();

        return new InternalUserResponse(userCreatedId, user.getUserName(), user.getPhoneNumber(), user.getEmail(),
                grade.getGradeName(), grade.getPointRate(), point, addresses);
    }

}
