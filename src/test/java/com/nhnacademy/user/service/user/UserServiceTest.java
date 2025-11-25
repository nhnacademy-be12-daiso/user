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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.PointResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import com.nhnacademy.user.entity.user.UserStatusHistory;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.user.GradeRepository;
import com.nhnacademy.user.repository.user.StatusRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.repository.user.UserStatusHistoryRepository;
import com.nhnacademy.user.service.point.PointService;
import com.nhnacademy.user.service.user.impl.UserServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private UserGradeHistoryRepository userGradeHistoryRepository;

    @Mock
    private UserStatusHistoryRepository userStatusHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PointService pointService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Account testAccount;
    private String testLoginId = "testUser";
    private LocalDate testBirthDate = LocalDate.of(2003, 11, 7);

    @BeforeEach
    void setUp() {
        testUser = new User("테스트", "010-1234-5678", "test@test.com", testBirthDate);
        testAccount = new Account(testLoginId, "encodedPassword", Role.USER, testUser);
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

        Grade generalGrade = new Grade("GENERAL", BigDecimal.ONE);
        Status activeStatus = new Status("ACTIVE");

        given(gradeRepository.findByGradeName("GENERAL")).willReturn(Optional.of(generalGrade));
        given(statusRepository.findByStatusName("ACTIVE")).willReturn(Optional.of(activeStatus));

        userService.signUp(request);

        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
        verify(userGradeHistoryRepository).save(any(UserGradeHistory.class));
        verify(userStatusHistoryRepository).save(any(UserStatusHistory.class));
        verify(pointService).earnPointByPolicy(request.loginId(), "REGISTER");
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
        given(accountRepository.findByIdWithUser(testLoginId)).willReturn(Optional.of(testAccount));

        Grade grade = new Grade("GOLD", BigDecimal.valueOf(2.5));
        UserGradeHistory gradeHistory = new UserGradeHistory(testUser, grade, "승급");
        given(userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(testUser))
                .willReturn(Optional.of(gradeHistory));

        Status status = new Status("ACTIVE");
        UserStatusHistory statusHistory = new UserStatusHistory(testUser, status);
        given(userStatusHistoryRepository.findTopByUserOrderByChangedAtDesc(testUser))
                .willReturn(Optional.of(statusHistory));

        given(pointService.getCurrentPoint(testLoginId)).willReturn(new PointResponse(BigDecimal.valueOf(5000)));

        UserResponse response = userService.getUserInfo(testLoginId);

        assertThat(response).isNotNull();
        assertThat(response.userName()).isEqualTo("테스트");
        assertThat(response.gradeName()).isEqualTo("GOLD");
        assertThat(response.statusName()).isEqualTo("ACTIVE");
        assertThat(response.point()).isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
    void test6() {
        given(accountRepository.findByIdWithUser("wrong")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo("wrong"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void test7() {
        UserModifyRequest request = new UserModifyRequest("수정된 이름",
                "010-1234-5678", "new@new.com", testBirthDate);

        given(accountRepository.findByIdWithUser(testLoginId)).willReturn(Optional.of(testAccount));

        userService.modifyUserInfo(testLoginId, request);

        assertThat(testUser.getUserName()).isEqualTo("수정된 이름");
        assertThat(testUser.getEmail()).isEqualTo("new@new.com");
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 존재하지 않는 유저")
    void test8() {
        UserModifyRequest request = new UserModifyRequest("a", "b", "c", testBirthDate);

        given(accountRepository.findByIdWithUser("wrong")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.modifyUserInfo("wrong", request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    void test9() {
        PasswordModifyRequest request = new PasswordModifyRequest("oldPwd", "newPwd");

        Account mockAccount = mock(Account.class);

        given(accountRepository.findByIdWithUser(testLoginId)).willReturn(Optional.of(mockAccount));

        given(mockAccount.getPassword()).willReturn("encodedOld");
        given(passwordEncoder.matches("oldPwd", "encodedOld")).willReturn(true);
        given(passwordEncoder.encode("newPwd")).willReturn("encodedNew");

        userService.modifyUserPassword(testLoginId, request);

        verify(mockAccount).modifyPassword("encodedNew");
    }

    @Test
    @DisplayName("비밀번호 수정 실패 - 존재하지 않는 유저")
    void test10() {
        PasswordModifyRequest request = new PasswordModifyRequest("any", "any");

        given(accountRepository.findByIdWithUser("wrong")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.modifyUserPassword("wrong", request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("로그인 시 마지막 접속일 갱신")
    void test11() {
        given(accountRepository.findByIdWithUser(testLoginId)).willReturn(Optional.of(testAccount));

        userService.modifyLastLoginAt(testLoginId);

        assertThat(testUser.getLastLoginAt()).isNotNull();
    }

}
