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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy.user.dto.request.UserGradeRequest;
import com.nhnacademy.user.dto.request.UserStatusRequest;
import com.nhnacademy.user.dto.response.PointResponse;
import com.nhnacademy.user.dto.response.UserDetailResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import com.nhnacademy.user.entity.user.UserStatusHistory;
import com.nhnacademy.user.repository.user.GradeRepository;
import com.nhnacademy.user.repository.user.StatusRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.repository.user.UserStatusHistoryRepository;
import com.nhnacademy.user.service.point.PointService;
import com.nhnacademy.user.service.user.impl.AdminServiceImpl;
import java.math.BigDecimal;
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
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private UserStatusHistoryRepository userStatusHistoryRepository;

    @Mock
    private UserGradeHistoryRepository userGradeHistoryRepository;

    @Mock
    private PointService pointService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User mockUser;
    private Account mockAccount;
    private Status mockStatus;
    private Grade mockGrade;
    private UserStatusHistory mockStatusHistory;
    private UserGradeHistory mockGradeHistory;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        mockAccount = mock(Account.class);
        mockStatus = mock(Status.class);
        mockGrade = mock(Grade.class);
        mockStatusHistory = mock(UserStatusHistory.class);
        mockGradeHistory = mock(UserGradeHistory.class);
    }

    @Test
    @DisplayName("전체 회원 목록 조회 - 성공")
    void test1() {
        Pageable pageable = PageRequest.of(0, 10);

        UserResponse userResponse = new UserResponse(
                "testUser",
                "홍길동",
                "010-1234-5678",
                "test@email.com",
                LocalDate.now(),
                "GENERAL",
                BigDecimal.valueOf(1000),
                "ACTIVE",
                LocalDateTime.now()
        );

        Page<UserResponse> responsePage = new PageImpl<>(List.of(userResponse), pageable, 1);

        given(userRepository.findAllUser(pageable)).willReturn(responsePage);

        Page<UserResponse> result = adminService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).loginId()).isEqualTo("testUser");
        assertThat(result.getContent().get(0).statusName()).isEqualTo("ACTIVE");
        assertThat(result.getContent().get(0).point()).isEqualTo(BigDecimal.valueOf(1000));

        verify(userRepository).findAllUser(pageable);
    }

    @Test
    @DisplayName("특정 회원 상세 조회 - 성공")
    void test2() {
        Long userId = 1L;

        given(mockUser.getAccount()).willReturn(mockAccount);
        given(mockAccount.getLoginId()).willReturn("detailUser");
        given(mockAccount.getRole()).willReturn(Role.USER);
        given(mockUser.getUserCreatedId()).willReturn(userId);

        given(userRepository.findByIdWithAccount(userId)).willReturn(Optional.of(mockUser));

        given(userStatusHistoryRepository.findTopByUserOrderByChangedAtDesc(mockUser)).willReturn(
                Optional.of(mockStatusHistory));
        given(mockStatusHistory.getStatus()).willReturn(mockStatus);
        given(mockStatus.getStatusName()).willReturn("ACTIVE");

        given(userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(mockUser)).willReturn(
                Optional.of(mockGradeHistory));
        given(mockGradeHistory.getGrade()).willReturn(mockGrade);
        given(mockGrade.getGradeName()).willReturn("VIP");

        given(pointService.getCurrentPoint(userId)).willReturn(new PointResponse(BigDecimal.TEN));

        UserDetailResponse response = adminService.getUserDetail(userId);

        assertThat(response.loginId()).isEqualTo("detailUser");
        assertThat(response.gradeName()).isEqualTo("VIP");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("회원 상태 변경 - 성공 (ACTIVE -> BANNED)")
    void test3() {
        Long adminId = 999L;
        Long targetUserId = 1L;
        UserStatusRequest request = new UserStatusRequest("BANNED");

        given(userRepository.findByIdWithAccount(targetUserId)).willReturn(Optional.of(mockUser));
        given(statusRepository.findByStatusName("BANNED")).willReturn(Optional.of(mockStatus));

        adminService.modifyUserStatus(adminId, targetUserId, request);

        verify(userStatusHistoryRepository).save(any(UserStatusHistory.class));
    }

    @Test
    @DisplayName("회원 등급 변경 - 성공 (GENERAL -> GOLD)")
    void test4() {
        Long adminId = 999L;
        Long targetUserId = 1L;
        UserGradeRequest request = new UserGradeRequest("GOLD");

        given(userRepository.findByIdWithAccount(targetUserId)).willReturn(Optional.of(mockUser));
        given(gradeRepository.findByGradeName("GOLD")).willReturn(Optional.of(mockGrade));

        adminService.modifyUserGrade(adminId, targetUserId, request);

        verify(userGradeHistoryRepository).save(any(UserGradeHistory.class));
    }

    @Test
    @DisplayName("회원 상태 변경 실패 - 존재하지 않는 상태값")
    void test5() {
        Long adminId = 999L;
        Long targetUserId = 1L;
        UserStatusRequest request = new UserStatusRequest("WEIRD_STATUS");

        given(userRepository.findByIdWithAccount(targetUserId)).willReturn(Optional.of(mockUser));
        given(statusRepository.findByStatusName("WEIRD_STATUS")).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.modifyUserStatus(adminId, targetUserId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("존재하지 않는 상태");
    }

}
