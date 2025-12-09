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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.nhnacademy.user.dto.response.PointResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.point.PointService;
import com.nhnacademy.user.service.user.impl.InternalUserServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InternalUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGradeHistoryRepository userGradeHistoryRepository;

    @Mock
    private AccountStatusHistoryRepository accountStatusHistoryRepository;

    @Mock
    private PointService pointService;

    @InjectMocks
    private InternalUserServiceImpl internalUserService;

    private User testUser;
    private Account testAccount;
    private Long testUserId = 1L;

    @Test
    @DisplayName("내부 통신용 회원 정보 조회 (getInternalUserInfo)")
    void test1() {
        given(userRepository.findByIdWithAccount(testUserId)).willReturn(Optional.of(testUser));

        Status status = new Status("ACTIVE");

        AccountStatusHistory statusHistory = new AccountStatusHistory(testAccount, status);

        given(accountStatusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(testAccount))
                .willReturn(Optional.of(statusHistory));

        Grade grade = new Grade("GOLD", BigDecimal.valueOf(2.5));

        UserGradeHistory gradeHistory = new UserGradeHistory(testUser, grade, "reason");

        given(userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(testUser))
                .willReturn(Optional.of(gradeHistory));

        given(pointService.getCurrentPoint(any())).willReturn(new PointResponse(BigDecimal.valueOf(5000)));

        var response = internalUserService.getInternalUserInfo(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.userCreatedId()).isEqualTo(testUserId);
        assertThat(response.gradeName()).isEqualTo("GOLD");
        assertThat(response.point()).isEqualTo(BigDecimal.valueOf(5000));
    }

}
