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

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Mock
    AccountRepository accountRepository;

    @Mock
    AccountStatusHistoryRepository statusHistoryRepository;

    @Mock
    MailService mailService;

    @InjectMocks
    VerificationService verificationService;

    private Account mockAccount;

    private AccountStatusHistory mockHistory;

    @BeforeEach
    void setUp() {
        User mockUser = mock(User.class);
        mockAccount = mock(Account.class);
        mockHistory = mock(AccountStatusHistory.class);
        Status status = new Status("DORMANT");

        lenient().when(mockUser.getAccount()).thenReturn(mockAccount);
        lenient().when(mockAccount.getUser()).thenReturn(mockUser);
        lenient().when(mockHistory.getStatus()).thenReturn(status);
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
        given(statusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(any())).willReturn(Optional.of(mockHistory));
        given(mailService.sendMessage(anyString())).willReturn(code);

        verificationService.sendCode(userCreatedId);

        verify(valueOperations).set(eq("ACTIVE_CODE:" + userCreatedId), anyString(), anyLong(), any());
        verify(mailService).sendMessage(eq(email));
    }

    @Test
    @DisplayName("인증 번호 검증 실패 - 코드 불일치")
    void test2() {
        Long userCreatedId = 1L;
        String wrongCode = "000000";
        String correctCode = "123456";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        given(valueOperations.get("ACTIVE_CODE:" + userCreatedId)).willReturn(correctCode);

        assertThatThrownBy(() -> verificationService.verifyCode(userCreatedId, wrongCode))
                .isInstanceOf(InvalidCodeException.class);
    }

}
