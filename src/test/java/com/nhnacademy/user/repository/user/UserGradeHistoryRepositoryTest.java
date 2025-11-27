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

import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class UserGradeHistoryRepositoryTest {

    @Autowired
    private UserGradeHistoryRepository historyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("유저의 가장 최근 등급 변경 이력 조회")
    void test1() {
        User user = new User("gradeUser", "010-3333-4444", "grade@email.com", LocalDate.now());
        entityManager.persist(user);

        Grade general = new Grade("GENERAL", BigDecimal.valueOf(0.01));
        Grade vip = new Grade("VIP", BigDecimal.valueOf(0.05));
        entityManager.persist(general);
        entityManager.persist(vip);

        UserGradeHistory history1 = new UserGradeHistory(user, general, "가입");
        entityManager.persist(history1);

        UserGradeHistory history2 = new UserGradeHistory(user, vip, "승급");
        entityManager.persist(history2);

        entityManager.flush();
        entityManager.clear();

        Optional<UserGradeHistory> result = historyRepository.findTopByUserOrderByChangedAtDesc(user);

        assertThat(result).isPresent();
        assertThat(result.get().getGrade().getGradeName()).isEqualTo("VIP");
        assertThat(result.get().getReason()).isEqualTo("승급");
    }

}
