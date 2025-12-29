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

package com.nhnacademy.user.repository.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nhnacademy.user.config.QueryDslConfig;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(QueryDslConfig.class)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Account 저장 시 User 매핑 성공")
    void test1() {
        Grade grade = new Grade("GENERAL", BigDecimal.valueOf(1.0));
        entityManager.persist(grade);

        User user = new User("테스트_이름", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7), grade);
        userRepository.save(user);

        Status status = new Status("ACTIVE");
        entityManager.persist(status);

        Account account = new Account("test", "pwd123!@#", Role.USER, user, status);
        accountRepository.save(account);

        Account found = accountRepository.findById("test").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getLoginId()).isEqualTo("test");
        assertThat(found.getRole()).isEqualTo(Role.USER);
        assertThat(found.getUser().getUserName()).isEqualTo("테스트_이름");
        assertThat(found.getStatus().getStatusName()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("이미 계정이 있는 User가 계정을 또 만들었을 때 예외 발생")
    void test2() {
        Grade grade = new Grade("GENERAL", BigDecimal.valueOf(1.0));
        entityManager.persist(grade);

        User user = new User("테스트1", "010-0000-0000",
                "test1@test.com", LocalDate.of(2003, 11, 7), grade);
        userRepository.save(user);

        Status status = new Status("ACTIVE");
        entityManager.persist(status);

        Account account1 = new Account("test1", "pwd111!!!", Role.USER, user, status);
        accountRepository.save(account1);

        Account account2 = new Account("test2", "pwd222@@@", Role.USER, user, status);

        assertThatThrownBy(() -> accountRepository.saveAndFlush(account2));
    }

}
