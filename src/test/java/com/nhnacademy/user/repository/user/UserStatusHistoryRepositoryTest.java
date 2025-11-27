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

import com.nhnacademy.user.entity.user.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserStatusHistory;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class UserStatusHistoryRepositoryTest {

    @Autowired
    private UserStatusHistoryRepository historyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("유저의 가장 최근 상태 변경 이력 조회")
    void test1() {
        User user = new User("testUser", "010-1111-2222", "test@email.com", LocalDate.now());
        entityManager.persist(user);

        Status active = new Status("ACTIVE");
        Status dormant = new Status("DORMANT");
        entityManager.persist(active);
        entityManager.persist(dormant);

        UserStatusHistory history1 = new UserStatusHistory(user, active);
        entityManager.persist(history1);

        UserStatusHistory history2 = new UserStatusHistory(user, dormant);
        entityManager.persist(history2);

        entityManager.flush();
        entityManager.clear();

        Optional<UserStatusHistory> result = historyRepository.findTopByUserOrderByChangedAtDesc(user);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus().getStatusName()).isEqualTo("DORMANT");

        assertThat(result.get().getStatus()).isNotNull();
    }

}
