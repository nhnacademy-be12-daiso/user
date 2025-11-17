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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.LoginRequest;
import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.properties.JwtProperties;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.user.impl.UserServiceImpl;
import com.nhnacademy.user.util.JwtUtil;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

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

        given(userRepository.existsByPhoneNumber(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(accountRepository.existsByLoginId(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

        given(userRepository.save(any(User.class))).willReturn(testUser);

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

        Authentication mockAuthentication = mock(Authentication.class);

        given(accountRepository.findByIdWithUser("test")).willReturn(Optional.of(testAccount));
        given(authenticationManager.authenticate(any())).willReturn(mockAuthentication);
        given(jwtUtil.createAccessToken(testLoginId, "USER")).willReturn("Daiso token");

        String token = userService.login(request);

        assertThat(token).isEqualTo("Daiso token");
        assertThat(testUser.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("로그인 실패 - 인증 실패")
    void test6() {
        LoginRequest request = new LoginRequest("test", "wrong");

        given(authenticationManager.authenticate(any())).willThrow();

        assertThatThrownBy(() -> userService.login(request));
    }

    @Test
    @DisplayName("로그아웃 성공 - 토큰 블랙리스트 등록")
    void test7() {
        String token = "valid-token";
        String header = "Daiso " + token;

        given(jwtProperties.getTokenPrefix()).willReturn("Daiso");
        given(jwtUtil.getRemainingExpiration(token)).willReturn(3600000L);
        given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

        userService.logout(header);

        verify(jwtUtil).getRemainingExpiration(token);
        verify(valueOperations).set(token, "logout", 3600000L, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("로그아웃 성공 - 이미 만료된 토큰 (Redis 등록 안 함)")
    void test8() {
        String token = "expired-token";
        String header = "Daiso " + token;

        given(jwtProperties.getTokenPrefix()).willReturn("Daiso");
        given(jwtUtil.getRemainingExpiration(token)).willReturn(0L);

        userService.logout(header);

        verify(jwtUtil).getRemainingExpiration(token);
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void test9() {
        given(accountRepository.findByIdWithUser(testLoginId)).willReturn(Optional.of(testAccount));

        UserResponse response = userService.getUserInfo(testLoginId);

        assertThat(response).isNotNull();
        assertThat(response.userName()).isEqualTo("테스트");
        assertThat(response.email()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
    void test10() {
        given(accountRepository.findByIdWithUser("wrong")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo("wrong"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void test11() {
        UserModifyRequest request = new UserModifyRequest("수정된 이름",
                "010-1234-5678", "new@new.com", testBirthDate);

        User spyUser = mock(User.class);
        Account spyAccount = new Account(testLoginId, "pwd", Role.USER, spyUser);

        given(accountRepository.findByIdWithUser(testLoginId)).willReturn(Optional.of(spyAccount));

        userService.modifyUserInfo(testLoginId, request);

        verify(accountRepository).findByIdWithUser(testLoginId);
        verify(spyUser).modifyInfo(request.userName(), request.phoneNumber(), request.email(), request.birth());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 존재하지 않는 유저")
    void test12() {
        UserModifyRequest request = new UserModifyRequest("a", "b", "c", testBirthDate);

        given(accountRepository.findByIdWithUser("wrong")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.modifyUserInfo("wrong", request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    void test13() {
        PasswordModifyRequest request = new PasswordModifyRequest("pwd111!!!", "new123!@#");

        String encodedNewPassword = "ENCODED_NEW_PASSWORD";

        Account mockAccount = mock(Account.class);

        given(accountRepository.findByIdWithUser(testLoginId)).willReturn(Optional.of(mockAccount));
        given(mockAccount.getPassword()).willReturn("ENCODED_CURRENT_PASSWORD");
        given(passwordEncoder.matches("pwd111!!!", "ENCODED_CURRENT_PASSWORD")).willReturn(true);
        given(passwordEncoder.encode("new123!@#")).willReturn(encodedNewPassword);

        userService.modifyUserPassword(testLoginId, request);

        verify(passwordEncoder).matches("pwd111!!!", "ENCODED_CURRENT_PASSWORD");
        verify(passwordEncoder).encode("new123!@#");
        verify(mockAccount).modifyPassword(encodedNewPassword);
    }

    @Test
    @DisplayName("비밀번호 수정 실패 - 존재하지 않는 유저")
    void test15() {
        PasswordModifyRequest request = new PasswordModifyRequest("any", "any");

        given(accountRepository.findByIdWithUser("wrong")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.modifyUserPassword("wrong", request))
                .isInstanceOf(UserNotFoundException.class);
    }

}
