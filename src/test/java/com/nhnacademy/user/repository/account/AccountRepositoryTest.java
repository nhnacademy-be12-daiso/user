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
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import(QueryDslConfig.class)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    AccountStatusHistoryRepository historyRepository;

    @Test
    @DisplayName("Account 저장 시 User 매핑 성공")
    void test1() {
        User user = new User("테스트_이름", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7));
        userRepository.save(user);

        Account account = new Account("test", "pwd123!@#", Role.USER, user);
        accountRepository.save(account);

        Account found = accountRepository.findById("test").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getLoginId()).isEqualTo("test");
        assertThat(found.getRole()).isEqualTo(Role.USER);
        assertThat(found.getUser().getUserName()).isEqualTo("테스트_이름");
    }

    @Test
    @DisplayName("이미 계정이 있는 User가 계정을 또 만들었을 때 예외 발생")
    void test2() {
        User user = new User("테스트1", "010-0000-0000",
                "test1@test.com", LocalDate.of(2003, 11, 7));
        userRepository.save(user);

        Account account1 = new Account("test1", "pwd111!!!", Role.USER, user);
        accountRepository.save(account1);

        Account account2 = new Account("test2", "pwd222@@@", Role.USER, user);

        assertThatThrownBy(() -> accountRepository.saveAndFlush(account2));
    }

    @Test
    @DisplayName("휴면 전환 대상자(1년 이상 미접속 + 현재 ACTIVE) 조회")
    void test3() {
        Status active = new Status("ACTIVE");
        statusRepository.save(active);

        Status dormant = new Status("DORMANT");
        statusRepository.save(dormant);

        Account targetAccount = createAccount("target", "010-1111-1111", "t@t.com", "targetId");
        setLastLogin(targetAccount, LocalDateTime.now().minusYears(2));
        historyRepository.save(new AccountStatusHistory(targetAccount, active));

        Account recent = createAccount("recent", "010-2222-2222", "r@r.com", "recentId");
        setLastLogin(recent, LocalDateTime.now().minusDays(1));
        historyRepository.save(new AccountStatusHistory(recent, active));

        Account alreadyDormant = createAccount("dormant", "010-3333-3333", "d@d.com", "dormantId");
        setLastLogin(alreadyDormant, LocalDateTime.now().minusYears(2));
        historyRepository.save(new AccountStatusHistory(alreadyDormant, dormant));

        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(1);
        List<Account> result = accountRepository.findDormantAccounts(cutoffDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLoginId()).isEqualTo("targetId");
        assertThat(result.get(0).getUser().getUserName()).isEqualTo("target");
    }

    private Account createAccount(String name, String phone, String email, String loginId) {
        User user = new User(name, phone, email, LocalDate.now());
        userRepository.save(user);

        Account account = new Account(loginId, "pwd123!@#", Role.USER, user);

        return accountRepository.save(account);
    }

    private void setLastLogin(Account account, LocalDateTime time) {
        ReflectionTestUtils.setField(account, "lastLoginAt", time);
        accountRepository.save(account);
    }

}
