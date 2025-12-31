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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.address.Address;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.account.AccountWithdrawnException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.address.AddressRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.user.impl.InternalUserServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InternalUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private InternalUserServiceImpl internalUserService;

    @Test
    @DisplayName("내부 통신용 회원 정보 조회 (getInternalUserInfo)")
    void test1() {
        Grade goldGrade = new Grade("GOLD", BigDecimal.valueOf(2.5));
        Status activeStatus = new Status("ACTIVE");
        User user = new User("홍길동", "010-1234-5678", "test@test.com", LocalDate.of(1990, 1, 1), goldGrade);
        Account account = new Account("testId", "rawPassword", Role.USER, user, activeStatus);

        ReflectionTestUtils.setField(user, "currentPoint", 5000L);
        ReflectionTestUtils.setField(user, "account", account);

        given(userRepository.findByIdWithAccount(anyLong())).willReturn(Optional.of(user));

        given(addressRepository.findAllByUser(any(User.class))).willReturn(Collections.emptyList());

        var response = internalUserService.getInternalUserInfo(1L);

        assertThat(response.gradeName()).isEqualTo("GOLD");
        assertThat(response.point()).isEqualTo(5000L);
        assertThat(response.pointRate()).isEqualTo(BigDecimal.valueOf(2.5));
        assertThat(response.addresses()).isEmpty();
    }

    @Test
    @DisplayName("내부 통신용 회원 정보 조회 - 주소가 있는 경우")
    void test2() {
        Grade grade = new Grade("GOLD", BigDecimal.valueOf(2.5));
        Status status = new Status("ACTIVE");
        User user = new User("홍길동", "010-1234-5678", "t@t.com", LocalDate.now(), grade);
        Account account = new Account("testId", "pw", Role.USER, user, status);
        ReflectionTestUtils.setField(user, "account", account);

        Address address = mock(Address.class);
        given(address.getAddressId()).willReturn(10L);
        given(address.getAddressName()).willReturn("집");
        given(address.getZipCode()).willReturn("12345");
        given(address.getRoadAddress()).willReturn("서울시");
        given(address.getAddressDetail()).willReturn("상세");
        given(address.isDefault()).willReturn(true);

        given(userRepository.findByIdWithAccount(anyLong())).willReturn(Optional.of(user));
        given(addressRepository.findAllByUser(any(User.class))).willReturn(List.of(address));

        var response = internalUserService.getInternalUserInfo(1L);

        assertThat(response.addresses()).hasSize(1);
        assertThat(response.addresses().getFirst().addressName()).isEqualTo("집");
    }

    @Test
    @DisplayName("회원 유효성 검증 (existsUser) - 존재함")
    void test3() {
        given(userRepository.existsById(1L)).willReturn(true);

        boolean result = internalUserService.existsUser(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("내부 통신용 회원 정보 조회 실패 - 찾을 수 없는 회원")
    void test4() {
        given(userRepository.findByIdWithAccount(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> internalUserService.getInternalUserInfo(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("찾을 수 없는 회원입니다.");
    }

    @Test
    @DisplayName("내부 통신용 회원 정보 조회 실패 - 탈퇴한 계정")
    void test5() {
        User user = mock(User.class);
        Account account = mock(Account.class);
        Status withdrawnStatus = new Status("WITHDRAWN");

        given(userRepository.findByIdWithAccount(anyLong())).willReturn(Optional.of(user));
        given(user.getAccount()).willReturn(account);
        given(account.getStatus()).willReturn(withdrawnStatus);

        assertThatThrownBy(() -> internalUserService.getInternalUserInfo(1L))
                .isInstanceOf(AccountWithdrawnException.class)
                .hasMessage("이미 탈퇴한 계정입니다.");
    }

}
