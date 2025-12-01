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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.account.StatusRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.user.impl.UserServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StatusRepository statusRepository;

    @Mock
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

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
    @DisplayName("계정 탈퇴 - 상태 변경 이력 저장 확인")
    void test1() {
        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));

        Status withdrawnStatus = new Status("WITHDRAWN");
        given(statusRepository.findByStatusName("WITHDRAWN")).willReturn(Optional.of(withdrawnStatus));

        userService.withdrawUser(testUserId);

        verify(accountStatusHistoryRepository, times(1)).save(any(AccountStatusHistory.class));
    }

}
