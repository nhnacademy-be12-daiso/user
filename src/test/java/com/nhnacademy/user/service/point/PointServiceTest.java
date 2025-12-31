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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.PointRequest;
import com.nhnacademy.user.dto.response.PointHistoryResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.point.Method;
import com.nhnacademy.user.entity.point.PointHistory;
import com.nhnacademy.user.entity.point.PointPolicy;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.point.InvalidPointInputException;
import com.nhnacademy.user.exception.point.PointNotEnoughException;
import com.nhnacademy.user.exception.point.PointPolicyNotFoundException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.point.PointHistoryRepository;
import com.nhnacademy.user.repository.point.PointPolicyRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.point.impl.PointServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @DisplayName("포인트 적립 성공 - 정률(RATIO)")
    void test2() {
        given(userRepository.findByIdForUpdate(testUserId)).willReturn(Optional.of(user));

        PointPolicy policy = new PointPolicy("구매적립", "ORDER", Method.RATIO, BigDecimal.valueOf(0.1));
        given(pointPolicyRepository.findByPolicyType("ORDER")).willReturn(Optional.of(policy));

        pointService.earnPointByPolicy(testUserId, "ORDER", BigDecimal.valueOf(10000));

        verify(pointHistoryRepository).save(any(PointHistory.class));
        assertThat(user.getCurrentPoint()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("포인트 적립 실패 - 존재하지 않는 정책")
    void test3() {
        given(userRepository.findByIdForUpdate(testUserId)).willReturn(Optional.of(user));
        given(pointPolicyRepository.findByPolicyType("UNKNOWN")).willReturn(Optional.empty());

        assertThatThrownBy(() -> pointService.earnPointByPolicy(testUserId, "UNKNOWN"))
                .isInstanceOf(PointPolicyNotFoundException.class)
                .hasMessage("존재하지 않는 포인트 정책입니다.");
    }

    @Test
    @DisplayName("포인트 적립 실패 - 정률 정책인데 기준 금액 오류")
    void test4() {
        given(userRepository.findByIdForUpdate(testUserId)).willReturn(Optional.of(user));
        PointPolicy policy = new PointPolicy("구매", "ORDER", Method.RATIO, BigDecimal.valueOf(0.1));
        given(pointPolicyRepository.findByPolicyType("ORDER")).willReturn(Optional.of(policy));

        assertThatThrownBy(() -> pointService.earnPointByPolicy(testUserId, "ORDER", null))
                .isInstanceOf(InvalidPointInputException.class)
                .hasMessageContaining("기준 금액이 필수");

        assertThatThrownBy(() -> pointService.earnPointByPolicy(testUserId, "ORDER", BigDecimal.ZERO))
                .isInstanceOf(InvalidPointInputException.class)
                .hasMessageContaining("기준 금액이 필수");
    }

    @Test
    @DisplayName("수동 포인트 사용 성공")
    void test5() {
        ReflectionTestUtils.setField(user, "currentPoint", 5000L);
        given(userRepository.findByIdForUpdate(testUserId)).willReturn(Optional.of(user));

        PointRequest request = new PointRequest(testUserId, 1000L, Type.USE, "상품 구매 사용");

        pointService.processPoint(request);

        verify(pointHistoryRepository).save(any(PointHistory.class));
        assertThat(user.getCurrentPoint()).isEqualTo(4000L);
    }

    @Test
    @DisplayName("수동 포인트 사용 실패 - 현재 포인트가 Null인 경우 (0원으로 취급)")
    void test6() {
        ReflectionTestUtils.setField(user, "currentPoint", null);
        given(userRepository.findByIdForUpdate(testUserId)).willReturn(Optional.of(user));

        PointRequest request = new PointRequest(testUserId, 100L, Type.USE, "사용");

        assertThatThrownBy(() -> pointService.processPoint(request))
                .isInstanceOf(PointNotEnoughException.class)
                .hasMessage("포인트 잔액이 부족합니다.");
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    void test7() {
        ReflectionTestUtils.setField(user, "currentPoint", 100L);
        given(userRepository.findByIdForUpdate(testUserId)).willReturn(Optional.of(user));

        PointRequest request = new PointRequest(testUserId, 1000L, Type.USE, "사용");

        assertThatThrownBy(() -> pointService.processPoint(request))
                .isInstanceOf(PointNotEnoughException.class);
    }

    @Test
    @DisplayName("내 포인트 내역 조회 성공")
    void test8() {
        Pageable pageable = PageRequest.of(0, 10);
        PointHistory history = new PointHistory(user, 500L, Type.EARN, "테스트 적립");
        Page<PointHistory> historyPage = new PageImpl<>(List.of(history));

        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(user));
        given(pointHistoryRepository.findAllByUserOrderByCreatedAtDesc(user, pageable)).willReturn(historyPage);

        Page<PointHistoryResponse> result = pointService.getMyPointHistory(testUserId, pageable);

        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().getFirst().amount()).isEqualTo(500L);
    }

    @Test
    @DisplayName("회원 조회 실패")
    void test9() {
        given(userRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> pointService.earnPointByPolicy(99L, "REGISTER"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("찾을 수 없는 회원입니다.");
    }

}
