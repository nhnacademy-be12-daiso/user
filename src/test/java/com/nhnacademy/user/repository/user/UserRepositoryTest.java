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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nhnacademy.user.entity.user.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserStatusHistory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    UserStatusHistoryRepository historyRepository;

    @Test
    @DisplayName("User 저장 및 조회 성공")
    void test1() {
        User user = new User("테스트_이름", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7));
        userRepository.save(user);

        User found = userRepository.findById(user.getUserCreatedId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getUserName()).isEqualTo("테스트_이름");
        assertThat(found.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(found.getEmail()).isEqualTo("test@test.com");
        assertThat(found.getBirth()).isEqualTo(LocalDate.of(2003, 11, 7));
    }

    @Test
    @DisplayName("중복된 연락처 저장 시 예외 발생")
    void test2() {
        User user1 = new User("테스트1", "010-0000-0000",
                "test1@test.com", LocalDate.of(2003, 11, 7));
        userRepository.save(user1);

        User user2 = new User("테스트2", "010-0000-0000",
                "test2@test.com", LocalDate.now());

        assertThatThrownBy(() -> userRepository.save(user2));
    }

    @Test
    @DisplayName("중복된 이메일 저장 시 예외 발생")
    void test3() {
        User user1 = new User("테스트1", "010-1111-1111",
                "test@test.com", LocalDate.of(2003, 11, 7));
        userRepository.save(user1);

        User user2 = new User("테스트2", "010-2222-2222",
                "test@test.com", LocalDate.now());

        assertThatThrownBy(() -> userRepository.save(user2));
    }

    @Test
    @DisplayName("휴면 전환 대상자(1년 이상 미접속 + 현재 ACTIVE) 조회")
    void test4() {
        Status active = new Status("ACTIVE");
        Status dormant = new Status("DORMANT");
        statusRepository.save(active);
        statusRepository.save(dormant);

        User targetUser = createUser("target", "010-1111-1111", "t@t.com");
        setLastLogin(targetUser, LocalDateTime.now().minusYears(2));
        historyRepository.save(new UserStatusHistory(targetUser, active));

        User recentUser = createUser("recent", "010-2222-2222", "r@r.com");
        setLastLogin(recentUser, LocalDateTime.now().minusDays(1));
        historyRepository.save(new UserStatusHistory(recentUser, active));

        User dormantUser = createUser("dormant", "010-3333-3333", "d@d.com");
        setLastLogin(dormantUser, LocalDateTime.now().minusYears(2));
        historyRepository.save(new UserStatusHistory(dormantUser, dormant));

        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(1);
        List<User> result = userRepository.findDormantUser(cutoffDate);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserName()).isEqualTo("target");
    }

    private User createUser(String name, String phone, String email) {
        User user = new User(name, phone, email, LocalDate.now());
        return userRepository.save(user);
    }

    private void setLastLogin(User user, LocalDateTime time) {
        ReflectionTestUtils.setField(user, "lastLoginAt", time);
        userRepository.save(user);
    }

}
