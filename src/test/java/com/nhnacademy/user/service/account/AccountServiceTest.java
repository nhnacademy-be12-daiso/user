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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.account.StateNotFoundException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.account.StatusRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.account.impl.AccountServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    @Mock
    private StatusRepository statusRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    @DisplayName("로그인 아이디 존재 여부 확인 - 존재함")
    void test1() {
        given(accountRepository.existsById("existId")).willReturn(true);
        boolean result = accountService.existsLoginId("existId");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("로그인 아이디 존재 여부 확인 - 존재하지 않음")
    void test2() {
        given(accountRepository.existsById("noneId")).willReturn(false);
        boolean result = accountService.existsLoginId("noneId");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("휴면 계정 활성화 성공")
    void test3() {
        Long userCreatedId = 1L;
        User mockUser = mock(User.class);
        Account mockAccount = mock(Account.class);
        Status activeStatus = new Status("ACTIVE");

        given(userRepository.findByIdWithAccount(userCreatedId)).willReturn(Optional.of(mockUser));
        given(mockUser.getAccount()).willReturn(mockAccount);
        given(statusRepository.findByStatusName("ACTIVE")).willReturn(Optional.of(activeStatus));

        accountService.activeUser(userCreatedId);

        verify(accountStatusHistoryRepository).save(any(AccountStatusHistory.class));
        verify(mockAccount).modifyStatus(activeStatus);
    }

    @Test
    @DisplayName("휴면 계정 활성화 실패 - 존재하지 않는 회원")
    void test4() {
        Long userCreatedId = 999L;
        given(userRepository.findByIdWithAccount(userCreatedId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.activeUser(userCreatedId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("찾을 수 없는 회원");
    }

    @Test
    @DisplayName("휴면 계정 활성화 실패 - ACTIVE 상태 데이터 없음(시스템 오류)")
    void test5() {
        Long userCreatedId = 1L;
        User mockUser = mock(User.class);
        Account mockAccount = mock(Account.class);

        given(userRepository.findByIdWithAccount(userCreatedId)).willReturn(Optional.of(mockUser));
        given(mockUser.getAccount()).willReturn(mockAccount);
        given(statusRepository.findByStatusName("ACTIVE")).willReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.activeUser(userCreatedId))
                .isInstanceOf(StateNotFoundException.class)
                .hasMessageContaining("존재하지 않는 상태");
    }

}
