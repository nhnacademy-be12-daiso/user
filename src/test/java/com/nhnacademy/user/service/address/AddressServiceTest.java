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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.AddressRequest;
import com.nhnacademy.user.dto.response.AddressResponse;
import com.nhnacademy.user.entity.address.Address;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.address.AddressLimitExceededException;
import com.nhnacademy.user.exception.address.AddressNotFoundException;
import com.nhnacademy.user.exception.address.DefaultAddressDeletionException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.address.AddressRepository;
import com.nhnacademy.user.repository.user.UserRepository;
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
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Long testUserId = 1L;
    private AddressRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("테스트", "010-1234-5678", "test@nhn.com", LocalDate.now());
        ReflectionTestUtils.setField(testUser, "userCreatedId", testUserId);

        testRequest = new AddressRequest("조선대학교", "61452", "광주광역시 동구 조선대길 146", "1층", true);

        lenient().when(userRepository.findByIdWithAccount(testUserId)).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("주소 등록 성공 (기본 배송지 설정)")
    void test1() {
        given(addressRepository.countByUser(testUser)).willReturn(5L);
        given(addressRepository.save(any(Address.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        addressService.addAddress(testUserId, testRequest);

        verify(addressRepository).clearAllDefaultsByUser(testUser);
        verify(addressRepository).save(any(Address.class));
    }


    @Test
    @DisplayName("주소 등록 실패 - 주소 10개 초과")
    void test2() {
        given(addressRepository.countByUser(testUser)).willReturn(10L);

        assertThatThrownBy(() -> addressService.addAddress(testUserId, testRequest))
                .isInstanceOf(AddressLimitExceededException.class)
                .hasMessage("최대 10개의 주소만 등록할 수 있습니다.");

        verify(addressRepository, never()).save(any());
    }

    @Test
    @DisplayName("주소 등록 실패 - 존재하지 않는 유저")
    void test3() {
        given(userRepository.findByIdWithAccount(999L)).willReturn(Optional.empty());

        AddressRequest request = new AddressRequest("테스트", "12345", "주소", "상세", false);

        assertThatThrownBy(() -> addressService.addAddress(999L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("주소 목록 조회 성공")
    void test4() {
        Address addr1 = new Address(testUser, "집", "12345", "광주", "1층", true);
        Address addr2 = new Address(testUser, "회사", "09876", "판교", "1층", false);

        ReflectionTestUtils.setField(addr1, "addressId", 1L);
        ReflectionTestUtils.setField(addr2, "addressId", 2L);

        given(addressRepository.findAllByUser(testUser))
                .willReturn(List.of(addr1, addr2));

        List<AddressResponse> responses = addressService.getMyAddresses(testUserId);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).addressName()).isEqualTo("집");
        assertThat(responses.get(0).addressId()).isEqualTo(1L);
        assertThat(responses.get(1).addressName()).isEqualTo("회사");
    }

    @Test
    @DisplayName("주소 수정 성공")
    void test5() {
        Address originalAddress = new Address(testUser, "옛날 별칭", "12345", "옛날 주소", "1층", false);
        AddressRequest modifyRequest = new AddressRequest("새 별칭", "09876", "새로운 주소", "1층", true);

        given(addressRepository.findByAddressIdAndUser(1L, testUser))
                .willReturn(Optional.of(originalAddress));

        addressService.modifyAddress(testUserId, 1L, modifyRequest);

        verify(addressRepository).clearAllDefaultsByUser(testUser);

        assertThat(originalAddress.getAddressName()).isEqualTo("새 별칭");
        assertThat(originalAddress.getAddressDetail()).isEqualTo("1층");
        assertThat(originalAddress.isDefault()).isTrue();
    }

    @Test
    @DisplayName("주소 수정 실패 - 존재하지 않는 주소")
    void test6() {
        given(addressRepository.findByAddressIdAndUser(anyLong(), any(User.class)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.modifyAddress(testUserId, 99L, testRequest))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessage("찾을 수 없는 주소입니다.");
    }

    @Test
    @DisplayName("주소 삭제 성공")
    void test7() {
        Address address = new Address(testUser, "집", "12345", "광주", "1층", false);

        given(addressRepository.findByAddressIdAndUser(1L, testUser))
                .willReturn(Optional.of(address));

        addressService.deleteAddress(testUserId, 1L);

        verify(addressRepository).delete(address);
    }

    @Test
    @DisplayName("주소 삭제 실패 - 존재하지 않는 주소")
    void test8() {
        given(addressRepository.findByAddressIdAndUser(99L, testUser))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddress(testUserId, 99L))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, never()).delete(any());
    }

    @Test
    @DisplayName("주소 삭제 실패 - 기본 배송지는 삭제 불가")
    void test9() {
        Address address = new Address(testUser, "집", "12345", "광주", "1층", true);

        given(addressRepository.findByAddressIdAndUser(1L, testUser))
                .willReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.deleteAddress(testUserId, 1L))
                .isInstanceOf(DefaultAddressDeletionException.class)
                .hasMessage("기본 배송지는 삭제할 수 없습니다.");

        verify(addressRepository, never()).delete(any());
    }

    @Test
    @DisplayName("주소가 0개일 때 추가하면 자동으로 기본 배송지 설정")
    void test10() {
        given(addressRepository.countByUser(testUser)).willReturn(0L);
        given(addressRepository.save(any(Address.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AddressRequest request = new AddressRequest("집", "123", "주소", "상세", false);

        addressService.addAddress(testUserId, request);

        verify(addressRepository).save(argThat(addr -> addr.isDefault() == true));
    }

}
