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

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.user.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.jpa.JpaSystemException;

@DataJpaTest
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원, 계정 저장 성공")
    void test1() {
        User user = new User("test-name", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7));
        userRepository.save(user);

        Account account = new Account("test", "test-password", user);
        accountRepository.save(account);

        Account found = accountRepository.findById("test").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getLoginId()).isEqualTo("test");
        assertThat(found.getUser()).isNotNull();
        assertThat(found.getUser().getUserName()).isEqualTo("test-name");
    }

    @Test
    @DisplayName("이미 존재하는 로그인 ID로 가입 시 예외 발생")
    void test2() {
        User user1 = new User("test-name1", "010-1111-1111",
                "test1@test.com", LocalDate.of(2011, 1, 1));
        userRepository.save(user1);

        Account account1 = new Account("exist-id", "password1", user1);
        accountRepository.save(account1);

        User user2 = new User("test-name2", "010-2222-2222",
                "test2@test.com", LocalDate.of(2022, 2, 2));
        userRepository.save(user2);

        Account account2 = new Account("exist-id", "password2", user2);

        assertThatThrownBy(() -> accountRepository.save(account2))
                .isInstanceOf(JpaSystemException.class);
    }

}
