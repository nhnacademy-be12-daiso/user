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

import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.PointResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.account.StatusRepository;
import com.nhnacademy.user.repository.address.AddressRepository;
import com.nhnacademy.user.repository.user.GradeRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.point.PointService;
import com.nhnacademy.user.service.user.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private UserGradeHistoryRepository userGradeHistoryRepository;

    @Mock
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PointService pointService;

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
        SignupRequest request = new SignupRequest("test", "pwd123!@#", "테스트",
                "010-1234-5678", "test@test.com", LocalDate.of(2003, 11, 7));

        given(accountRepository.existsByLoginId(anyString())).willReturn(false);
        given(userRepository.existsByPhoneNumber(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);

        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        Grade generalGrade = new Grade("GENERAL", BigDecimal.ONE);
        Status activeStatus = new Status("ACTIVE");

        given(gradeRepository.findByGradeName("GENERAL")).willReturn(Optional.of(generalGrade));
        given(statusRepository.findByStatusName("ACTIVE")).willReturn(Optional.of(activeStatus));

        userService.signUp(request);

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

        given(accountRepository.existsByLoginId(request.loginId())).willReturn(true);

        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("이미 존재하는 아이디입니다.");

        verify(accountRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void test5() {
        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));

        Grade grade = new Grade("GOLD", BigDecimal.valueOf(2.5));
        UserGradeHistory gradeHistory = new UserGradeHistory(testUser, grade, "승급");
        given(userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(testUser))
                .willReturn(Optional.of(gradeHistory));

        Status status = new Status("ACTIVE");
        AccountStatusHistory statusHistory = new AccountStatusHistory(testAccount, status);
        given(accountStatusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(testAccount))
                .willReturn(Optional.of(statusHistory));

        given(pointService.getCurrentPoint(testUserId)).willReturn(new PointResponse(BigDecimal.valueOf(5000)));

        UserResponse response = userService.getUserInfo(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.loginId()).isEqualTo(testLoginId);
        assertThat(response.userName()).isEqualTo("테스트");
        assertThat(response.gradeName()).isEqualTo("GOLD");
        assertThat(response.point()).isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
    void test6() {
        given(userRepository.findByIdWithAccount(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void test7() {
        UserModifyRequest request = new UserModifyRequest("수정된 이름",
                "010-1234-5678", "new@new.com", testBirthDate);

        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));

        userService.modifyUserInfo(testUserId, request);

        assertThat(testUser.getUserName()).isEqualTo("수정된 이름");
        assertThat(testUser.getEmail()).isEqualTo("new@new.com");
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    void test8() {
        PasswordModifyRequest request = new PasswordModifyRequest("oldPwd", "newPwd");

        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));

        Account mockAccount = mock(Account.class);
        ReflectionTestUtils.setField(testUser, "account", mockAccount);

        given(mockAccount.getPassword()).willReturn("encodedOld");
        given(passwordEncoder.matches("oldPwd", "encodedOld")).willReturn(true);
        given(passwordEncoder.encode("newPwd")).willReturn("encodedNew");

        userService.modifyUserPassword(testUserId, request);

        verify(mockAccount).modifyPassword("encodedNew");
    }

    @Test
    @DisplayName("내부 통신용 회원 정보 조회 (getInternalUserInfo)")
    void test9() {
        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));

        Status status = new Status("ACTIVE");

        AccountStatusHistory statusHistory = new AccountStatusHistory(testAccount, status);

        given(accountStatusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(testAccount))
                .willReturn(Optional.of(statusHistory));

        Grade grade = new Grade("GOLD", BigDecimal.valueOf(2.5));

        UserGradeHistory gradeHistory = new UserGradeHistory(testUser, grade, "reason");

        given(userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(testUser))
                .willReturn(Optional.of(gradeHistory));

        given(pointService.getCurrentPoint(any())).willReturn(new PointResponse(BigDecimal.valueOf(5000)));
        given(addressRepository.findFirstByUserAndIsDefaultTrue(testUser)).willReturn(Optional.empty());

        var response = userService.getInternalUserInfo(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.userCreatedId()).isEqualTo(testUserId);
        assertThat(response.gradeName()).isEqualTo("GOLD");
        assertThat(response.point()).isEqualTo(BigDecimal.valueOf(5000));
    }

}
