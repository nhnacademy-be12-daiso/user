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

package com.nhnacademy.user.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.payco.PaycoLoginResponse;
import com.nhnacademy.user.dto.payco.PaycoSignUpRequest;
import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.account.AccountWithdrawnException;
import com.nhnacademy.user.exception.user.PasswordNotMatchException;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.account.StatusRepository;
import com.nhnacademy.user.repository.user.GradeRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.point.PointService;
import com.nhnacademy.user.service.user.impl.UserServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserGradeHistoryRepository userGradeHistoryRepository;

    @Mock
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PointService pointService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Account testAccount;
    private Long testUserId = 1L;
    private String testLoginId = "testUser";
    private LocalDate testBirthDate = LocalDate.of(2003, 11, 7);

    @BeforeEach
    void setUp() {
        testUser = new User("테스트", "010-1234-5678", "test@test.com", testBirthDate);
        ReflectionTestUtils.setField(testUser, "userCreatedId", testUserId);

        testAccount = new Account(testLoginId, "encodedPassword", Role.USER, testUser);

        ReflectionTestUtils.setField(testUser, "account", testAccount);
    }

    @Test
    @DisplayName("회원가입 성공")
    void test1() {
        SignupRequest request = new SignupRequest("testId", "testPw123!", "홍길동", "010-1234-5678", "test@test.com",
                LocalDate.of(1990, 1, 1));
        Grade generalGrade = new Grade("GENERAL", BigDecimal.valueOf(1.0));
        Status activeStatus = new Status("ACTIVE");

        given(accountRepository.existsById(anyString())).willReturn(false);
        given(userRepository.existsByPhoneNumber(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(gradeRepository.findByGradeName("GENERAL")).willReturn(Optional.of(generalGrade));
        given(statusRepository.findByStatusName("ACTIVE")).willReturn(Optional.of(activeStatus));

        User savedUser =
                new User(request.userName(), request.phoneNumber(), request.email(), request.birth(), generalGrade);
        ReflectionTestUtils.setField(savedUser, "userCreatedId", 1L);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        userService.signUp(request);

        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 연락처")
    void test2() {
        SignupRequest request = new SignupRequest("test", "pwd123!@#", "테스트",
                "010-1234-5678", "test@test.com", LocalDate.of(2003, 11, 7));

        given(userRepository.existsByPhoneNumber(request.phoneNumber())).willReturn(true);

        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("이미 존재하는 연락처입니다.");

        verify(userRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void test3() {
        SignupRequest request = new SignupRequest("test", "pwd123!@#", "테스트",
                "010-1234-5678", "test@test.com", LocalDate.of(2003, 11, 7));

        given(userRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("이미 존재하는 이메일입니다.");

        verify(userRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 아이디")
    void test4() {
        SignupRequest request = new SignupRequest("test", "pwd123!@#", "테스트",
                "010-1234-5678", "test@test.com", LocalDate.of(2003, 11, 7));

        given(accountRepository.existsById(request.loginId())).willReturn(true);

        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("이미 존재하는 아이디입니다.");

        verify(accountRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void test5() {
        Long userCreatedId = 1L;
        Grade grade = new Grade("GENERAL", BigDecimal.valueOf(1.0));
        Status status = new Status("ACTIVE");

        User user = new User("홍길동", "010-1234-5678", "test@test.com", LocalDate.of(1990, 1, 1), grade);
        Account account = new Account("testId", "pw", Role.USER, user, status);

        ReflectionTestUtils.setField(user, "userCreatedId", userCreatedId);
        ReflectionTestUtils.setField(user, "currentPoint", 1000L);
        ReflectionTestUtils.setField(user, "account", account);
        ReflectionTestUtils.setField(account, "joinedAt", LocalDateTime.now());

        given(userRepository.findByIdWithAccount(userCreatedId)).willReturn(Optional.of(user));

        UserResponse response = userService.getUserInfo(userCreatedId);

        assertThat(response.userName()).isEqualTo("홍길동");
        assertThat(response.point()).isEqualTo(1000L);
        assertThat(response.gradeName()).isEqualTo("GENERAL");
        assertThat(response.statusName()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
    void test6() {
        given(userRepository.findByIdWithAccount(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 탈퇴한 계정")
    void test7() {
        Status withdrawnStatus = new Status("WITHDRAWN");
        User withdrawnUser = new User("탈퇴자", "010-0000-0000", "w@w.com", LocalDate.now(), null);
        Account withdrawnAccount = new Account("wid", "pw", Role.USER, withdrawnUser, withdrawnStatus);
        ReflectionTestUtils.setField(withdrawnUser, "account", withdrawnAccount);

        given(userRepository.findByIdWithAccount(anyLong())).willReturn(Optional.of(withdrawnUser));

        assertThatThrownBy(() -> userService.getUserInfo(1L))
                .isInstanceOf(AccountWithdrawnException.class)
                .hasMessage("이미 탈퇴한 계정입니다.");
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void test8() {
        UserModifyRequest request = new UserModifyRequest("수정된 이름",
                "010-1234-5678", "new@test.com", testBirthDate);

        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));

        userService.modifyUserInfo(testUserId, request);

        assertThat(testUser.getUserName()).isEqualTo("수정된 이름");
        assertThat(testUser.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 다른 사람이 쓰고 있는 연락처")
    void test9() {
        UserModifyRequest request = new UserModifyRequest("이름", "010-9999-9999", "my@test.com", LocalDate.now());

        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));
        given(userRepository.existsByPhoneNumber(request.phoneNumber())).willReturn(true);

        assertThatThrownBy(() -> userService.modifyUserInfo(testUserId, request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("이미 존재하는 연락처입니다.");
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 다른 사람이 쓰고 있는 이메일")
    void test10() {
        UserModifyRequest request = new UserModifyRequest("이름", "010-1234-5678", "duplicate@test.com", LocalDate.now());

        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> userService.modifyUserInfo(testUserId, request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    void test11() {
        PasswordModifyRequest request = new PasswordModifyRequest("oldPwd", "newPwd");

        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));

        Account mockAccount = mock(Account.class);
        ReflectionTestUtils.setField(testUser, "account", mockAccount);

        given(mockAccount.getPassword()).willReturn("encodedOld");
        given(passwordEncoder.matches("oldPwd", "encodedOld")).willReturn(true);
        given(passwordEncoder.encode("newPwd")).willReturn("encodedNew");

        userService.modifyAccountPassword(testUserId, request);

        verify(mockAccount).modifyPassword("encodedNew");
    }

    @Test
    @DisplayName("비밀번호 수정 실패 - 현재 비밀번호 불일치")
    void test12() {
        PasswordModifyRequest request = new PasswordModifyRequest("wrongOldPw", "newPw");

        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));

        Account mockAccount = mock(Account.class);
        ReflectionTestUtils.setField(testUser, "account", mockAccount);
        given(mockAccount.getPassword()).willReturn("encodedRealPw");

        given(passwordEncoder.matches("wrongOldPw", "encodedRealPw")).willReturn(false);

        assertThatThrownBy(() -> userService.modifyAccountPassword(testUserId, request))
                .isInstanceOf(PasswordNotMatchException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void test13() {
        Status withdrawnStatus = new Status("WITHDRAWN");
        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));
        given(statusRepository.findByStatusName("WITHDRAWN")).willReturn(Optional.of(withdrawnStatus));

        Account accountSpy = mock(Account.class);
        ReflectionTestUtils.setField(testUser, "account", accountSpy);

        userService.withdrawUser(testUserId);

        verify(accountStatusHistoryRepository).save(any(AccountStatusHistory.class));
        verify(accountSpy).modifyStatus(withdrawnStatus);
    }

    @Test
    @DisplayName("Payco 신규 회원가입")
    void test14() {
        PaycoSignUpRequest request = new PaycoSignUpRequest("PAYCO_12345");
        Grade generalGrade = new Grade("GENERAL", BigDecimal.valueOf(1.0));
        Status activeStatus = new Status("ACTIVE");

        given(accountRepository.findById(anyString())).willReturn(Optional.empty());
        given(gradeRepository.findByGradeName("GENERAL")).willReturn(Optional.of(generalGrade));
        given(statusRepository.findByStatusName("ACTIVE")).willReturn(Optional.of(activeStatus));

        User savedUser = new User("Payco User", "010-0000-0000", "payco@test.com", LocalDate.now(), generalGrade);
        ReflectionTestUtils.setField(savedUser, "userCreatedId", 1L);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        PaycoLoginResponse response = userService.findOrCreatePaycoUser(request);

        assertThat(response.isNewUser()).isTrue();
        verify(userGradeHistoryRepository).save(any());
        verify(pointService).earnPointByPolicy(any(), anyString());
    }

    @Test
    @DisplayName("Payco 기존 회원 로그인")
    void test15() {
        PaycoSignUpRequest request = new PaycoSignUpRequest("PAYCO_12345");
        String expectedLoginId = "PAYCO_PAYCO_12345";

        Grade grade = new Grade("GENERAL", BigDecimal.valueOf(1.0));
        Status activeStatus = new Status("ACTIVE");
        User existingUser = new User("Payco User", "010", "a@a.com", LocalDate.now(), grade);
        Account existingAccount = new Account(expectedLoginId, "pw", Role.USER, existingUser, activeStatus);

        given(accountRepository.findById(expectedLoginId)).willReturn(Optional.of(existingAccount));

        PaycoLoginResponse response = userService.findOrCreatePaycoUser(request);

        assertThat(response.isNewUser()).isFalse();
        assertThat(response.loginId()).isEqualTo(expectedLoginId);
    }

    @Test
    @DisplayName("Payco 기존 회원 로그인 실패 - 탈퇴한 계정")
    void test16() {
        PaycoSignUpRequest request = new PaycoSignUpRequest("PAYCO_12345");
        String loginId = "PAYCO_PAYCO_12345";

        Account existingAccount = mock(Account.class);
        Status withdrawnStatus = new Status("WITHDRAWN");

        given(accountRepository.findById(loginId)).willReturn(Optional.of(existingAccount));
        given(existingAccount.getStatus()).willReturn(withdrawnStatus);

        assertThatThrownBy(() -> userService.findOrCreatePaycoUser(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("탈퇴한 계정입니다.");
    }

    @Test
    @DisplayName("생일자 조회 테스트")
    void test17() {
        // Slice 리턴값 Mocking이 복잡하므로 호출 여부만 검증
        Pageable pageable = Pageable.ofSize(10);

        userService.findByBirthdayMonth(11, pageable);

        verify(userRepository).findBirthdayUsersActive(11, 1L, pageable);
    }

}
