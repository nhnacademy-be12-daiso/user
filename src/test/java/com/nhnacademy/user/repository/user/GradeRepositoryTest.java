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

package com.nhnacademy.user.repository.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.nhnacademy.user.config.QueryDslConfig;
import com.nhnacademy.user.entity.user.Grade;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(QueryDslConfig.class)
class GradeRepositoryTest {

    @Autowired
    private GradeRepository gradeRepository;

    @Test
    @DisplayName("등급 이름으로 조회 성공")
    void test1() {
        Grade grade = new Grade("VIP", BigDecimal.valueOf(0.05));
        gradeRepository.save(grade);

        Optional<Grade> result = gradeRepository.findByGradeName("VIP");

        assertThat(result).isPresent();
        assertThat(result.get().getGradeName()).isEqualTo("VIP");
        assertThat(result.get().getPointRate()).isEqualByComparingTo(BigDecimal.valueOf(0.05));
    }

}
