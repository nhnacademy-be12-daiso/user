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
import com.nhnacademy.user.entity.user.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserStatusHistory;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.repository.user.UserStatusHistoryRepository;
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

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Mock
    UserRepository userRepository;

    @Mock
    UserStatusHistoryRepository statusHistoryRepository;

    @Mock
    DoorayMessageSender doorayMessageSender;

    @InjectMocks
    VerificationService verificationService;

    private User mockUser;

    private Account mockAccount;

    private UserStatusHistory mockHistory;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        mockAccount = mock(Account.class);
        mockHistory = mock(UserStatusHistory.class);
        Status status = new Status("DORMANT");

        lenient().when(mockUser.getAccount()).thenReturn(mockAccount);
        lenient().when(mockAccount.getUser()).thenReturn(mockUser);
        lenient().when(mockHistory.getStatus()).thenReturn(status);
    }

    @Test
    @DisplayName("인증 번호 발송 - 성공")
    void test1() {
        Long userCreatedId = 1L;
        String loginId = "testuser";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        given(userRepository.findByIdWithAccount(userCreatedId)).willReturn(Optional.of(mockUser));
        given(statusHistoryRepository.findTopByUserOrderByChangedAtDesc(any())).willReturn(Optional.of(mockHistory));
        given(mockAccount.getLoginId()).willReturn(loginId);

        verificationService.sendCode(userCreatedId);

        verify(valueOperations).set(eq("ACTIVE_CODE:" + userCreatedId), anyString(), anyLong(), any());
        verify(doorayMessageSender).send(eq(loginId), anyString());
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
