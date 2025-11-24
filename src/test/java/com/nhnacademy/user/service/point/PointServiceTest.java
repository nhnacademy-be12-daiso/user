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

package com.nhnacademy.user.service.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.nhnacademy.user.dto.response.PointResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.point.PointHistoryRepository;
import com.nhnacademy.user.repository.point.PointPolicyRepository;
import com.nhnacademy.user.service.point.impl.PointServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

    @Mock
    PointPolicyRepository pointPolicyRepository;

    @InjectMocks
    PointServiceImpl pointService;

    User user;
    Account account;

    @BeforeEach
    void setUp() {
        user = new User("User", "010-1111-2222", "a@a.com", LocalDate.now());
        account = new Account("testId", "pw", Role.USER, user);
    }

    @Test
    @DisplayName("현재 포인트 잔액 조회 (DB 계산 결과 반환)")
    void test1() {
        given(accountRepository.findByIdWithUser("testId")).willReturn(Optional.of(account));
        given(pointHistoryRepository.getPointByUser(user)).willReturn(1000L);

        PointResponse response = pointService.getCurrentPoint("testId");

        assertThat(response.currentPoint()).isEqualTo(1000L);
    }

//    @Test
//    @DisplayName("정책으로 포인트 적립")
//    void test2() {
//        given(accountRepository.findByIdWithUser("testId")).willReturn(Optional.of(account));
//
//        PointPolicy policy = new PointPolicy();
//        ReflectionTestUtils.setField(policy, "policyName", "회원가입");
//        ReflectionTestUtils.setField(policy, "policyType", "REGISTER");
//        ReflectionTestUtils.setField(policy, "method", Method.AMOUNT);
//        ReflectionTestUtils.setField(policy, "earnPoint", BigDecimal.valueOf(5000));
//
//        given(pointPolicyRepository.findByPolicyType("REGISTER"))
//                .willReturn(Optional.of(policy));
//
//        pointService.earnPointByPolicy("testId", "REGISTER");
//
//        verify(pointHistoryRepository).save(any());
//    }

}
