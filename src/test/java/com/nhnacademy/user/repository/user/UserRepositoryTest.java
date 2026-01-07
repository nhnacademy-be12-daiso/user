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
import com.nhnacademy.user.dto.response.BirthdayUserResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.dto.search.UserSearchCriteria;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.account.StatusRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import(QueryDslConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StatusRepository statusRepository;

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
    }

    @Test
    @DisplayName("중복된 연락처 저장 시 예외 발생")
    void test2() {
        User user1 = new User("테스트1", "010-0000-0000",
                "test1@test.com", LocalDate.now(), defaultGrade);
        userRepository.save(user1);

        User user2 = new User("테스트2", "010-0000-0000",
                "test2@test.com", LocalDate.now(), defaultGrade);

        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("중복된 이메일 저장 시 예외 발생")
    void test3() {
        User user1 = new User("테스트1", "010-1111-1111",
                "test@test.com", LocalDate.of(2003, 11, 7), defaultGrade);
        userRepository.save(user1);

        User user2 = new User("테스트2", "010-2222-2222",
                "test@test.com", LocalDate.now(), defaultGrade);

        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("존재 여부 확인 (existsBy)")
    void test4() {
        User user = new User("존재확인", "010-9999-9999", "exist@test.com", LocalDate.now(), defaultGrade);
        userRepository.save(user);

        assertThat(userRepository.existsByPhoneNumber("010-9999-9999")).isTrue();
        assertThat(userRepository.existsByPhoneNumber("010-0000-0000")).isFalse();

        assertThat(userRepository.existsByEmail("exist@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("none@test.com")).isFalse();
    }

    @Test
    @DisplayName("User 조회 시 Account Fetch Join 확인")
    void test5() {
        User user = new User("페치조인", "010-5555-5555", "fetch@join.com", LocalDate.now(), defaultGrade);
        userRepository.save(user);

        Account account = new Account("fetch_id", "pass", Role.USER, user, defaultStatus);
        accountRepository.save(account);

        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findByIdWithAccount(user.getUserCreatedId()).orElseThrow();

        assertThat(foundUser.getAccount()).isNotNull();
        assertThat(foundUser.getAccount().getLoginId()).isEqualTo("fetch_id");
    }

    @Test
    @DisplayName("비관적 락(Pessimistic Lock) 조회")
    void test6() {
        User user = new User("락테스트", "010-7777-8888", "lock@test.com", LocalDate.now(), defaultGrade);
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> foundUser = userRepository.findByIdForUpdate(user.getUserCreatedId());
        assertThat(foundUser).isPresent();
    }

    @Test
    @DisplayName("이름과 이메일로 회원 조회")
    void test9() {
        User user = new User("홍길동", "010-1234-1234", "hong@test.com", LocalDate.now(), defaultGrade);
        userRepository.save(user);

        Optional<User> found = userRepository.findByUserNameAndEmail("홍길동", "hong@test.com");
        assertThat(found).isPresent();

        Optional<User> notFound = userRepository.findByUserNameAndEmail("홍길동", "wrong@test.com");
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("QueryDSL - 전체 조회 및 검색 (키워드: 이름, 이메일, 로그인ID)")
    void test10() {
        User user1 = new User("김철수", "010-1111-1111", "chul@test.com", LocalDate.now(), defaultGrade);
        userRepository.save(user1);
        Account account1 = new Account("chulsu123", "pw", Role.USER, user1, defaultStatus);
        accountRepository.save(account1);

        User user2 = new User("박영희", "010-2222-2222", "young@test.com", LocalDate.now(), defaultGrade);
        userRepository.save(user2);
        Account account2 = new Account("younghee", "pw", Role.USER, user2, defaultStatus);
        accountRepository.save(account2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserResponse> searchByName = userRepository.findAllUser(pageable, new UserSearchCriteria("철수"));
        assertThat(searchByName.getContent()).hasSize(1);
        assertThat(searchByName.getContent().getFirst().userName()).isEqualTo("김철수");

        Page<UserResponse> searchByEmail = userRepository.findAllUser(pageable, new UserSearchCriteria("young"));
        assertThat(searchByEmail.getContent()).hasSize(1);
        assertThat(searchByEmail.getContent().getFirst().email()).isEqualTo("young@test.com");

        Page<UserResponse> searchById = userRepository.findAllUser(pageable, new UserSearchCriteria("chulsu"));
        assertThat(searchById.getContent()).hasSize(1);
        assertThat(searchById.getContent().getFirst().loginId()).isEqualTo("chulsu123");
    }

    @Test
    @DisplayName("QueryDSL - 동적 정렬 (가입일 joinedAt)")
    void test11() {
        User user1 = new User("유저1", "010-1111-1111", "u1@t.com", LocalDate.now(), defaultGrade);
        userRepository.save(user1);
        Account account1 = new Account("user1", "pw", Role.USER, user1, defaultStatus);
        ReflectionTestUtils.setField(account1, "joinedAt", LocalDateTime.now().minusDays(1));
        accountRepository.save(account1);

        User user2 = new User("유저2", "010-2222-2222", "u2@t.com", LocalDate.now(), defaultGrade);
        userRepository.save(user2);
        Account account2 = new Account("user2", "pw", Role.USER, user2, defaultStatus);
        ReflectionTestUtils.setField(account2, "joinedAt", LocalDateTime.now());
        accountRepository.save(account2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "joinedAt"));

        Page<UserResponse> result = userRepository.findAllUser(pageable, new UserSearchCriteria(null));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).loginId()).isEqualTo("user2");
        assertThat(result.getContent().get(1).loginId()).isEqualTo("user1");
    }

}
