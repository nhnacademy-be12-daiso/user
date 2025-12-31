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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.PointPolicyRequest;
import com.nhnacademy.user.dto.response.PointPolicyResponse;
import com.nhnacademy.user.entity.point.Method;
import com.nhnacademy.user.entity.point.PointPolicy;
import com.nhnacademy.user.exception.point.PointPolicyAlreadyExistsException;
import com.nhnacademy.user.exception.point.PointPolicyNotFoundException;
import com.nhnacademy.user.repository.point.PointPolicyRepository;
import com.nhnacademy.user.service.point.impl.PointPolicyServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PointPolicyServiceTest {

    @Mock
    private PointPolicyRepository pointPolicyRepository;

    @InjectMocks
    private PointPolicyServiceImpl pointPolicyService;

    @Test
    @DisplayName("정책 생성 - 성공 (정액)")
    void test1() {
        PointPolicyRequest request =
                new PointPolicyRequest("회원가입", "REGISTER", Method.AMOUNT, BigDecimal.valueOf(5000));

        given(pointPolicyRepository.existsByPolicyType("REGISTER")).willReturn(false);

        pointPolicyService.createPolicy(request);

        verify(pointPolicyRepository, times(1)).save(any(PointPolicy.class));
    }

    @Test
    @DisplayName("정책 생성 - 성공 (정률)")
    void test2() {
        PointPolicyRequest request = new PointPolicyRequest("주문적립", "ORDER", Method.RATIO, BigDecimal.valueOf(0.05));

        given(pointPolicyRepository.existsByPolicyType("ORDER")).willReturn(false);

        pointPolicyService.createPolicy(request);

        verify(pointPolicyRepository, times(1)).save(any(PointPolicy.class));
    }

    @Test
    @DisplayName("정책 생성 - 실패 (중복된 타입)")
    void test3() {
        PointPolicyRequest request =
                new PointPolicyRequest("회원가입", "REGISTER", Method.AMOUNT, BigDecimal.valueOf(5000));
        given(pointPolicyRepository.existsByPolicyType("REGISTER")).willReturn(true);

        assertThatThrownBy(() -> pointPolicyService.createPolicy(request))
                .isInstanceOf(PointPolicyAlreadyExistsException.class)
                .hasMessageContaining("이미 존재하는 정책입니다.");
    }

    @Test
    @DisplayName("정책 전체 조회 - 성공")
    void test4() {
        PointPolicy policy1 = new PointPolicy("가입", "REGISTER", Method.AMOUNT, BigDecimal.valueOf(1000));
        PointPolicy policy2 = new PointPolicy("주문", "ORDER", Method.RATIO, BigDecimal.valueOf(0.1));

        ReflectionTestUtils.setField(policy1, "pointPolicyId", 1L);
        ReflectionTestUtils.setField(policy2, "pointPolicyId", 2L);

        given(pointPolicyRepository.findAll()).willReturn(List.of(policy1, policy2));

        List<PointPolicyResponse> result = pointPolicyService.getPolicies();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).policyType()).isEqualTo("REGISTER");
        assertThat(result.get(0).method()).isEqualTo(Method.AMOUNT);

        assertThat(result.get(1).policyType()).isEqualTo("ORDER");
        assertThat(result.get(1).method()).isEqualTo(Method.RATIO);
    }

    @Test
    @DisplayName("정책 수정 - 성공")
    void test5() {
        Long policyId = 1L;
        PointPolicyRequest request =
                new PointPolicyRequest("리뷰수정", "REVIEW_MOD", Method.AMOUNT, BigDecimal.valueOf(0.01));
        PointPolicy mockPolicy = new PointPolicy("리뷰", "REVIEW", Method.AMOUNT, BigDecimal.valueOf(100));

        given(pointPolicyRepository.findById(policyId)).willReturn(Optional.of(mockPolicy));

        pointPolicyService.modifyPolicy(policyId, request);

        assertThat(mockPolicy.getPolicyName()).isEqualTo("리뷰수정");
        assertThat(mockPolicy.getEarnPoint()).isEqualTo(BigDecimal.valueOf(0.01));
        assertThat(mockPolicy.getMethod()).isEqualTo(Method.AMOUNT);
    }

    @Test
    @DisplayName("정책 삭제 - 실패 (존재하지 않음)")
    void test6() {
        Long policyId = 999L;

        given(pointPolicyRepository.existsById(policyId)).willReturn(false);

        assertThatThrownBy(() -> pointPolicyService.deletePolicy(policyId))
                .isInstanceOf(PointPolicyNotFoundException.class);
    }

}
