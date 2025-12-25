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

package com.nhnacademy.user.service.message;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.account.NotDormantAccountException;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.repository.account.AccountRepository;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @InjectMocks
    private VerificationService verificationService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MailService mailService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private Account mockAccount;
    private User mockUser;
    private Status dormantStatus;
    private Status activeStatus;

    @BeforeEach
    void setUp() {
        dormantStatus = new Status("DORMANT");
        activeStatus = new Status("ACTIVE");

        mockUser = mock(User.class);
        mockAccount = mock(Account.class);

        lenient().when(mockAccount.getUser()).thenReturn(mockUser);
        lenient().when(mockUser.getEmail()).thenReturn("test@test.com");
    }

    @Test
    @DisplayName("인증 번호 발송 - 성공")
    void test1() throws MessagingException, UnsupportedEncodingException {
        Long userCreatedId = 1L;
        String email = "test@test.com";
        String code = "123456";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(accountRepository.findByUser_UserCreatedId(userCreatedId)).willReturn(Optional.of(mockAccount));

        given(mockAccount.getStatus()).willReturn(dormantStatus);
        given(mailService.sendCode(anyString())).willReturn(code);

        verificationService.sendCode(userCreatedId);

        verify(valueOperations).set(eq("DORMANT_RELEASE_CODE:" + userCreatedId), anyString(), anyLong(), any());
        verify(mailService).sendCode(eq(email));
    }

    @Test
    @DisplayName("인증 번호 검증 실패 - 코드 불일치")
    void test2() {
        Long userCreatedId = 1L;
        String wrongCode = "000000";
        String correctCode = "123456";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        given(valueOperations.get("DORMANT_RELEASE_CODE:" + userCreatedId)).willReturn(correctCode);

        assertThatThrownBy(() -> verificationService.verifyCode(userCreatedId, wrongCode))
                .isInstanceOf(InvalidCodeException.class);
    }

    @Test
    @DisplayName("계정 상태 검증 실패 - 휴면 계정이 아님")
    void test3() {
        given(mockAccount.getStatus()).willReturn(activeStatus);

        assertThatThrownBy(() -> verificationService.validateDormantAccount(mockAccount))
                .isInstanceOf(NotDormantAccountException.class);
    }

}
