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
import com.nhnacademy.user.entity.point.PointHistory;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.user.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import(QueryDslConfig.class)
class PointHistoryRepositoryTest {

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("포인트 내역 페이징 조회 및 최신순 정렬 확인")
    void test1() {
        User user = new User("페이징유저", "010-5555-6666", "page@test.com", LocalDate.now());
        userRepository.save(user);

        for (int i = 1; i <= 15; i++) {
            pointHistoryRepository.save(new PointHistory(user, (long) i, Type.EARN, "적립 " + i));
        }

        Page<PointHistory> page = pointHistoryRepository.findAllByUserOrderByCreatedAtDesc(
                user, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);

        assertThat(page.getContent().get(0).getDescription()).isEqualTo("적립 15");
    }

}
