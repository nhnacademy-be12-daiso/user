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

package com.nhnacademy.user.service.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.AddressRequest;
import com.nhnacademy.user.dto.response.AddressResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.address.Address;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.address.AddressLimitExceededException;
import com.nhnacademy.user.exception.address.AddressNotFoundException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.address.AddressRepository;
import com.nhnacademy.user.service.address.impl.AddressServiceImpl;
import java.time.LocalDate;
import java.util.List;
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
public class AddressServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Account testAccount;
    private String testLoginId = "testUser";
    private AddressRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("테스트", "010-1234-5678", "test@nhn.com", LocalDate.now());
        testAccount = new Account(testLoginId, "password", Role.USER, testUser);
        testRequest = new AddressRequest("조선대학교", "광주광역시 동구 조선대길 146", "1층", true);
    }

    private void mockUserFind() {
        given(accountRepository.findByIdWithUser(testLoginId))
                .willReturn(Optional.of(testAccount));
    }

    @Test
    @DisplayName("주소 등록 성공 (기본 배송지 설정)")
    void test1() {
        mockUserFind();

        given(addressRepository.countByUser(testUser)).willReturn(5L);

        addressService.addAddress(testLoginId, testRequest);

        verify(addressRepository).clearAllDefaultsByUser(testUser);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("주소 등록 실패 - 주소 10개 초과")
    void test2() {
        mockUserFind();
        given(addressRepository.countByUser(testUser)).willReturn(10L);

        assertThatThrownBy(() -> addressService.addAddress(testLoginId, testRequest))
                .isInstanceOf(AddressLimitExceededException.class)
                .hasMessage("최대 10개의 주소만 등록할 수 있습니다.");

        verify(addressRepository, never()).save(any());
    }

    @Test
    @DisplayName("주소 등록 실패 - 존재하지 않는 유저")
    void test3() {
        given(accountRepository.findByIdWithUser(testLoginId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.addAddress(testLoginId, testRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("주소 목록 조회 성공")
    void test4() {
        mockUserFind();

        Address addr1 = new Address(testUser, "집", "광주", "1층", true);
        Address addr2 = new Address(testUser, "회사", "판교", "1층", false);

        ReflectionTestUtils.setField(addr1, "addressId", 1L);
        ReflectionTestUtils.setField(addr2, "addressId", 2L);

        given(addressRepository.findAllByUser(testUser))
                .willReturn(List.of(addr1, addr2));

        List<AddressResponse> responses = addressService.getMyAddresses(testLoginId);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).addressName()).isEqualTo("집");
        assertThat(responses.get(1).addressName()).isEqualTo("회사");
    }

    @Test
    @DisplayName("주소 수정 성공")
    void test5() {
        mockUserFind();

        Address originalAddress = new Address(testUser, "옛날 별칭", "옛날 주소", "1층", false);
        AddressRequest modifyRequest = new AddressRequest("새 별칭", "새로운 주소", "1층", true);

        given(addressRepository.findByAddressIdAndUser(1L, testUser))
                .willReturn(Optional.of(originalAddress));

        addressService.modifyAddress(testLoginId, 1L, modifyRequest);

        verify(addressRepository).clearAllDefaultsByUser(testUser);

        assertThat(originalAddress.getAddressName()).isEqualTo("새 별칭");
        assertThat(originalAddress.getAddressDetail()).isEqualTo("1층");
        assertThat(originalAddress.isDefault()).isTrue();
    }

    @Test
    @DisplayName("주소 수정 실패 - 존재하지 않는 주소")
    void test6() {
        mockUserFind();

        given(addressRepository.findByAddressIdAndUser(anyLong(), any(User.class)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.modifyAddress(testLoginId, 99L, testRequest))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessage("찾을 수 없는 주소입니다.");
    }

    @Test
    @DisplayName("주소 삭제 성공")
    void test7() {
        mockUserFind();

        Address address = new Address(testUser, "집", "광주", "1층", true);

        given(addressRepository.findByAddressIdAndUser(1L, testUser))
                .willReturn(Optional.of(address));

        addressService.deleteAddress(testLoginId, 1L);

        verify(addressRepository).delete(address);
    }

    @Test
    @DisplayName("주소 삭제 실패 - 존재하지 않는 주소")
    void test8() {
        mockUserFind();

        given(addressRepository.findByAddressIdAndUser(99L, testUser))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddress(testLoginId, 99L))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, never()).delete(any());
    }

}
