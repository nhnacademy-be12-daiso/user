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

package com.nhnacademy.user.repository.point;

import static org.assertj.core.api.Assertions.assertThat;

import com.nhnacademy.user.config.QueryDslConfig;
import com.nhnacademy.user.entity.point.Method;
import com.nhnacademy.user.entity.point.PointPolicy;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(QueryDslConfig.class)
class PointPolicyRepositoryTest {

    @Autowired
    private PointPolicyRepository pointPolicyRepository;

    @Test
    @DisplayName("정책 타입으로 조회 및 존재 여부 확인")
    void test1() {
        PointPolicy policy = new PointPolicy("회원가입", "REGISTER", Method.AMOUNT, BigDecimal.valueOf(5000));
        pointPolicyRepository.save(policy);

        Optional<PointPolicy> found = pointPolicyRepository.findByPolicyType("REGISTER");

        assertThat(found).isPresent();
        assertThat(found.get().getMethod()).isEqualTo(Method.AMOUNT);

        boolean exists = pointPolicyRepository.existsByPolicyType("REGISTER");
        assertThat(exists).isTrue();

        boolean notExists = pointPolicyRepository.existsByPolicyType("NON_EXIST");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("정책 이름으로 조회")
    void test2() {
        PointPolicy policy = new PointPolicy("리뷰작성", "REVIEW", Method.AMOUNT, BigDecimal.valueOf(500));
        pointPolicyRepository.save(policy);

        Optional<PointPolicy> found = pointPolicyRepository.findByPolicyName("리뷰작성");

        assertThat(found).isPresent();
    }

}
