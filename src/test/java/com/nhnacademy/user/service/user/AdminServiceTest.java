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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.AccountStatusRequest;
import com.nhnacademy.user.dto.request.UserGradeRequest;
import com.nhnacademy.user.dto.response.UserDetailResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.dto.search.UserSearchCriteria;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import com.nhnacademy.user.exception.account.StateNotFoundException;
import com.nhnacademy.user.exception.user.GradeNotFoundException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.account.StatusRepository;
import com.nhnacademy.user.repository.user.GradeRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.user.impl.AdminServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    @Mock
    private UserGradeHistoryRepository userGradeHistoryRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User mockUser;
    private Account mockAccount;
    private Status mockStatus;
    private Grade mockGrade;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        mockAccount = mock(Account.class);
        mockStatus = mock(Status.class);
        mockGrade = mock(Grade.class);

        lenient().when(mockUser.getAccount()).thenReturn(mockAccount);

        lenient().when(mockAccount.getStatus()).thenReturn(mockStatus);
        lenient().when(mockUser.getGrade()).thenReturn(mockGrade);

        lenient().when(mockAccount.getLoginId()).thenReturn("testUser");
        lenient().when(mockStatus.getStatusName()).thenReturn("ACTIVE");
        lenient().when(mockGrade.getGradeName()).thenReturn("GENERAL");
    }

    @Test
    @DisplayName("전체 회원 목록 조회 - 성공")
    void test1() {
        Pageable pageable = PageRequest.of(0, 10);

        UserResponse userResponse = new UserResponse(
                1L,
                "testUser",
                "홍길동",
                "010-1234-5678",
                "test@email.com",
                LocalDate.now(),
                "GENERAL",
                1000L,
                "ACTIVE",
                LocalDateTime.now()
        );

        Page<UserResponse> responsePage = new PageImpl<>(List.of(userResponse), pageable, 1);

        given(userRepository.findAllUser(pageable, new UserSearchCriteria("test"))).willReturn(responsePage);

        Page<UserResponse> result = adminService.getAllUsers(pageable, new UserSearchCriteria("test"));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().loginId()).isEqualTo("testUser");
        assertThat(result.getContent().getFirst().statusName()).isEqualTo("ACTIVE");
        assertThat(result.getContent().getFirst().point()).isEqualTo(1000L);

        verify(userRepository).findAllUser(pageable, new UserSearchCriteria("test"));
    }

    @Test
    @DisplayName("특정 회원 상세 조회 - 성공")
    void test2() {
        Long userCreatedId = 1L;
        given(userRepository.findByIdWithAccount(userCreatedId)).willReturn(Optional.of(mockUser));

        given(mockUser.getUserName()).willReturn("홍길동");
        given(mockAccount.getLoginId()).willReturn("testUser");
        given(mockAccount.getRole()).willReturn(Role.USER);
        given(mockAccount.getJoinedAt()).willReturn(LocalDateTime.now());

        UserDetailResponse response = adminService.getUserDetail(userCreatedId);

        assertThat(response.userName()).isEqualTo("홍길동");
        assertThat(response.loginId()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("특정 회원 상세 조회 실패 - 존재하지 않는 회원")
    void test3() {
        given(userRepository.findByIdWithAccount(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getUserDetail(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("찾을 수 없는 회원입니다.");
    }

    @Test
    @DisplayName("회원 상태 변경 - 성공 (ACTIVE -> BANNED)")
    void test4() {
        Long adminId = 999L;
        Long targetUserId = 1L;
        AccountStatusRequest request = new AccountStatusRequest("BANNED");

        given(userRepository.findByIdWithAccount(targetUserId)).willReturn(Optional.of(mockUser));
        given(mockUser.getAccount()).willReturn(mockAccount);
        given(statusRepository.findByStatusName("BANNED")).willReturn(Optional.of(mockStatus));

        adminService.modifyAccountStatus(adminId, targetUserId, request);

        verify(accountStatusHistoryRepository).save(any(AccountStatusHistory.class));
        verify(mockAccount).modifyStatus(mockStatus);
    }

    @Test
    @DisplayName("회원 상태 변경 실패 - 존재하지 않는 상태값")
    void test5() {
        Long adminId = 999L;
        Long targetUserId = 1L;
        AccountStatusRequest request = new AccountStatusRequest("WEIRD_STATUS");

        given(userRepository.findByIdWithAccount(targetUserId)).willReturn(Optional.of(mockUser));
        given(statusRepository.findByStatusName("WEIRD_STATUS")).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.modifyAccountStatus(adminId, targetUserId, request))
                .isInstanceOf(StateNotFoundException.class)
                .hasMessageContaining("존재하지 않는 상태");
    }

    @Test
    @DisplayName("회원 등급 변경 - 성공 (GENERAL -> GOLD)")
    void test6() {
        Long adminId = 999L;
        Long targetUserId = 1L;
        UserGradeRequest request = new UserGradeRequest("GOLD");

        given(userRepository.findByIdWithAccount(targetUserId)).willReturn(Optional.of(mockUser));
        given(gradeRepository.findByGradeName("GOLD")).willReturn(Optional.of(mockGrade));

        adminService.modifyUserGrade(adminId, targetUserId, request);

        verify(userGradeHistoryRepository).save(any(UserGradeHistory.class));
        verify(mockUser).modifyGrade(mockGrade);
    }

    @Test
    @DisplayName("회원 등급 변경 실패 - 존재하지 않는 회원")
    void test7() {
        UserGradeRequest request = new UserGradeRequest("GOLD");
        given(userRepository.findByIdWithAccount(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.modifyUserGrade(999L, 99L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("회원 등급 변경 실패 - 존재하지 않는 등급")
    void test8() {
        UserGradeRequest request = new UserGradeRequest("DIAMOND");

        given(userRepository.findByIdWithAccount(anyLong())).willReturn(Optional.of(mockUser));
        given(gradeRepository.findByGradeName("DIAMOND")).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.modifyUserGrade(999L, 1L, request))
                .isInstanceOf(GradeNotFoundException.class)
                .hasMessage("존재하지 않는 등급입니다.");
    }

}
