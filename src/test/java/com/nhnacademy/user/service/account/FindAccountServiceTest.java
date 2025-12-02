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

package com.nhnacademy.user.service.account;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.FindLoginIdRequest;
import com.nhnacademy.user.dto.request.FindPasswordRequest;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.account.impl.FindAccountServiceImpl;
import com.nhnacademy.user.service.message.MailService;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class FindAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @InjectMocks
    private FindAccountServiceImpl findAccountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User("홍길동", "010-1234-5678", "test@daiso.com", LocalDate.now());
        ReflectionTestUtils.setField(testUser, "userCreatedId", 1L);

        testAccount = new Account("testUser123", "encodedPwd", Role.USER, testUser);
        ReflectionTestUtils.setField(testUser, "account", testAccount);
    }

    @Test
    @DisplayName("아이디 찾기 - 성공 (마스킹 적용 확인)")
    void test1() {
        FindLoginIdRequest request = new FindLoginIdRequest("홍길동", "test@daiso.com");

        given(userRepository.findByUserNameAndEmail(request.userName(), request.email()))
                .willReturn(Optional.of(testUser));

        String result = findAccountService.findLoginId(request);

        assertThat(result).isEqualTo("testUser***");
    }

    @Test
    @DisplayName("아이디 찾기 - 실패 (회원 없음)")
    void test2() {
        FindLoginIdRequest request = new FindLoginIdRequest("없는사람", "no@daiso.com");

        given(userRepository.findByUserNameAndEmail(anyString(), anyString()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> findAccountService.findLoginId(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("찾을 수 없는 회원");
    }

    @Test
    @DisplayName("비밀번호 찾기 - 성공")
    void test3() throws Exception {
        FindPasswordRequest request = new FindPasswordRequest("testUser123", "홍길동", "test@daiso.com");

        given(accountRepository.findById(request.loginId())).willReturn(Optional.of(testAccount));
        given(passwordEncoder.encode(anyString())).willReturn("newEncodedPw");

        doNothing().when(mailService).sendTemporaryPassword(anyString(), anyString());

        findAccountService.createTemporaryPassword(request);

        verify(passwordEncoder, times(1)).encode(anyString());
        verify(mailService, times(1)).sendTemporaryPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호 찾기 - 실패 (계정 없음)")
    void test4() {
        FindPasswordRequest request = new FindPasswordRequest("unknownId", "홍길동", "test@daiso.com");

        given(accountRepository.findById(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> findAccountService.createTemporaryPassword(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("찾을 수 없는 계정");
    }

    @Test
    @DisplayName("비밀번호 찾기 - 실패 (이름 불일치)")
    void test5() {
        FindPasswordRequest request = new FindPasswordRequest("testUser123", "다른이름", "test@daiso.com");

        given(accountRepository.findById(request.loginId())).willReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> findAccountService.createTemporaryPassword(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하지 않습니다");
    }

    @Test
    @DisplayName("비밀번호 찾기 - 실패 (이메일 불일치)")
    void test6() {
        FindPasswordRequest request = new FindPasswordRequest("testUser123", "홍길동", "wrong@daiso.com");

        given(accountRepository.findById(request.loginId())).willReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> findAccountService.createTemporaryPassword(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하지 않습니다");
    }

    @Test
    @DisplayName("비밀번호 찾기 - 실패 (메일 전송 오류)")
    void test7() throws Exception {
        FindPasswordRequest request = new FindPasswordRequest("testUser123", "홍길동", "test@daiso.com");
        given(accountRepository.findById(request.loginId())).willReturn(Optional.of(testAccount));
        given(passwordEncoder.encode(anyString())).willReturn("newEncodedPw");

        doThrow(new RuntimeException("Mail Error")).when(mailService).sendTemporaryPassword(anyString(), anyString());

        assertThatThrownBy(() -> findAccountService.createTemporaryPassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("메일 발송 중 오류");
    }

}
