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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.LoginRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.UserAlreadyExistsException;
import com.nhnacademy.user.exception.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.user.impl.UserServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
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
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("회원가입 성공")
    void test1() {
        SignupRequest request = new SignupRequest("test", "pwd123!@#", "테스트",
                "010-1234-5678", "test@test.com", LocalDate.of(2003, 11, 7));

        given(userRepository.existsByPhoneNumber(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(accountRepository.existsByLoginId(anyString())).willReturn(false);

        User user = new User("테스트", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7));

        given(userRepository.save(any(User.class))).willReturn(user);

        userService.signUp(request);

        verify(passwordEncoder).encode("pwd123!@#");
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
    @DisplayName("로그인 성공")
    void test5() {
        LoginRequest request = new LoginRequest("test", "pwd123!@#");

        User user = new User("테스트", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7));
        Account account = new Account("test", "encoded", Role.USER, user);

        given(accountRepository.findById("test")).willReturn(Optional.of(account));
        given(passwordEncoder.matches("pwd123!@#", "encoded")).willReturn(true);

        userService.login(request);

        verify(accountRepository).findById("test");
        verify(passwordEncoder).matches("pwd123!@#", "encoded");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void test6() {
        LoginRequest request = new LoginRequest("test", "wrong");

        Account account = new Account("test", "encoded", Role.USER, null);

        given(accountRepository.findById("test")).willReturn(Optional.of(account));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

}
