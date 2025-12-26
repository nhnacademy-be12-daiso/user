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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.PointRequest;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.point.Method;
import com.nhnacademy.user.entity.point.PointPolicy;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.point.PointNotEnoughException;
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
        Grade grade = new Grade("GENERAL", BigDecimal.valueOf(1.0));
        user = new User("테스트", "010-1234-5678", "test@test.com", LocalDate.now(), grade);
        ReflectionTestUtils.setField(user, "userCreatedId", testUserId);

        Status status = new Status("ACTIVE");
        account = new Account("testId", "pwd123!@#", Role.USER, user, status);
    }

    @Test
    @DisplayName("정책으로 포인트 적립")
    void test1() {
        given(userRepository.findByIdForUpdate(testUserId)).willReturn(Optional.of(user));

        PointPolicy policy = new PointPolicy("회원가입", "REGISTER", Method.AMOUNT, BigDecimal.valueOf(5000));
        given(pointPolicyRepository.findByPolicyType("REGISTER")).willReturn(Optional.of(policy));

        pointService.earnPointByPolicy(testUserId, "REGISTER");

        verify(pointHistoryRepository).save(any());
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    void test2() {
        ReflectionTestUtils.setField(user, "currentPoint", 100L);
        given(userRepository.findByIdForUpdate(testUserId)).willReturn(Optional.of(user));

        PointRequest request = new PointRequest(testUserId, 1000L, Type.USE, "사용");

        assertThatThrownBy(() -> pointService.processPoint(request))
                .isInstanceOf(PointNotEnoughException.class);
    }

}
