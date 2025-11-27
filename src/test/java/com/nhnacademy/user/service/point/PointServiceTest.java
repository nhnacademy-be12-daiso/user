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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.response.PointResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.point.Method;
import com.nhnacademy.user.entity.point.PointPolicy;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.point.PointHistoryRepository;
import com.nhnacademy.user.repository.point.PointPolicyRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.point.impl.PointServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

    @Mock
    PointPolicyRepository pointPolicyRepository;

    @InjectMocks
    PointServiceImpl pointService;

    User user;
    Account account;
    Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        user = new User("User", "010-1111-2222", "a@a.com", LocalDate.now());
        ReflectionTestUtils.setField(user, "userCreatedId", testUserId);

        account = new Account("testId", "pw", Role.USER, user);
    }

    @Test
    @DisplayName("현재 포인트 잔액 조회 (DB 계산 결과 반환)")
    void test1() {
        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(user));
        given(pointHistoryRepository.getPointByUser(user)).willReturn(BigDecimal.valueOf(1000));

        PointResponse response = pointService.getCurrentPoint(testUserId);

        assertThat(response.currentPoint()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("정책으로 포인트 적립")
    void test2() {
        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(user));

        PointPolicy policy = new PointPolicy();
        ReflectionTestUtils.setField(policy, "policyName", "회원가입");
        ReflectionTestUtils.setField(policy, "policyType", "REGISTER");
        ReflectionTestUtils.setField(policy, "method", Method.AMOUNT);
        ReflectionTestUtils.setField(policy, "earnPoint", BigDecimal.valueOf(5000));

        given(pointPolicyRepository.findByPolicyType("REGISTER"))
                .willReturn(Optional.of(policy));

        pointService.earnPointByPolicy(testUserId, "REGISTER");

        verify(pointHistoryRepository).save(any());
    }

}
