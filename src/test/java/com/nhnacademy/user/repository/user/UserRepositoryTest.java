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

import com.nhnacademy.user.config.QueryDslConfig;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.account.AccountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(QueryDslConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Grade defaultGrade;

    private Status defaultStatus;

    @BeforeEach
    void setUp() {
        defaultGrade = new Grade("GENERAL", BigDecimal.valueOf(1.0));
        entityManager.persist(defaultGrade);

        defaultStatus = new Status("ACTIVE");
        entityManager.persist(defaultStatus);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("User 저장 및 조회 성공")
    void test1() {
        User user = new User("테스트_이름", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7), defaultGrade);
        userRepository.save(user);

        User found = userRepository.findById(user.getUserCreatedId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getUserName()).isEqualTo("테스트_이름");
        assertThat(found.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(found.getEmail()).isEqualTo("test@test.com");
        assertThat(found.getBirth()).isEqualTo(LocalDate.of(2003, 11, 7));
        assertThat(found.getGrade().getGradeName()).isEqualTo("GENERAL");
        assertThat(found.getCurrentPoint()).isEqualTo(0L);
    }

    @Test
    @DisplayName("중복된 연락처 저장 시 예외 발생")
    void test2() {
        User user1 = new User("테스트1", "010-0000-0000",
                "test1@test.com", LocalDate.of(2003, 11, 7), defaultGrade);
        userRepository.save(user1);

        User user2 = new User("테스트2", "010-0000-0000",
                "test2@test.com", LocalDate.now(), defaultGrade);

        assertThatThrownBy(() -> userRepository.save(user2));
    }

    @Test
    @DisplayName("중복된 이메일 저장 시 예외 발생")
    void test3() {
        User user1 = new User("테스트1", "010-1111-1111",
                "test@test.com", LocalDate.of(2003, 11, 7), defaultGrade);
        userRepository.save(user1);

        User user2 = new User("테스트2", "010-2222-2222",
                "test@test.com", LocalDate.now(), defaultGrade);

        assertThatThrownBy(() -> userRepository.save(user2));
    }

    @Test
    @DisplayName("User 조회 시 Account까지 한 번에 조회(Fetch Join)")
    void test4() {
        User user = new User(
                "페치조인", "010-5555-5555", "fetch@join.com", LocalDate.now(), defaultGrade);
        userRepository.save(user);

        Account account = new Account("fetch_id", "pass", Role.USER, user, defaultStatus);
        accountRepository.save(account);

        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findByIdWithAccount(user.getUserCreatedId()).orElseThrow();

        assertThat(foundUser.getAccount()).isNotNull();
        assertThat(foundUser.getAccount().getLoginId()).isEqualTo("fetch_id");
        assertThat(foundUser.getAccount().getStatus().getStatusName()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("비관적 락(Pessimistic Lock) 조회 쿼리 동작 확인")
    void test5() {
        User user = new User(
                "락테스트", "010-7777-8888", "lock@test.com", LocalDate.now(), defaultGrade);
        userRepository.save(user);

        entityManager.flush();
        entityManager.clear();

        Optional<User> foundUser = userRepository.findByIdForUpdate(user.getUserCreatedId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserName()).isEqualTo("락테스트");
    }

}
