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

package com.nhnacademy.user.service.user.impl;

import com.nhnacademy.user.dto.request.AccountStatusRequest;
import com.nhnacademy.user.dto.request.UserGradeRequest;
import com.nhnacademy.user.dto.response.UserDetailResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.dto.search.UserSearchCriteria;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
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
import com.nhnacademy.user.service.user.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final UserGradeHistoryRepository userGradeHistoryRepository;
    private final StatusRepository statusRepository;
    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    /**
     * (관리자용) 전체 회원 목록을 조회하는 메소드
     *
     * @param pageable 페이징 처리
     * @param criteria 검색에 사용되는 키워드
     * @return 페이징된 전체 회원 목록
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable, UserSearchCriteria criteria) {
        return userRepository.findAllUser(pageable, criteria);
    }

    /**
     * (관리자용) 특정 회원을 상세 조회하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @return Users 테이블 PK, 회원 정보 (이름, 연락처, 이메일, 생일, 등급, 보유 포인트), 계정 정보 (로그인 아이디, 상태, 권한, 가입일, 마지막 로그인 일시)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(Long userCreatedId) {
        User user = getUser(userCreatedId);

        return new UserDetailResponse(userCreatedId,
                user.getAccount().getLoginId(),
                user.getUserName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getBirth(),
                user.getAccount().getStatus().getStatusName(),
                user.getGrade().getGradeName(),
                user.getAccount().getRole().name(),
                user.getCurrentPoint(),
                user.getAccount().getJoinedAt(),
                user.getAccount().getLastLoginAt());
    }

    /**
     * (관리자용) 특정 계정의 상태를 변경하는 메소드
     *
     * @param adminId Users 테이블 PK
     * @param userId  Users 테이블 PK
     * @param request 변경할 상태 이름
     */
    @Override
    @Transactional
    public void modifyAccountStatus(Long adminId, Long userId, AccountStatusRequest request) {
        User user = getUser(userId);

        Account account = user.getAccount();

        Status newStatus = statusRepository.findByStatusName(request.statusName())
                .orElseThrow(() -> {
                    log.error("[AdminService] 계정 상태 변경 실패: 존재하지 않는 상태 ({})", request.statusName());
                    return new StateNotFoundException("존재하지 않는 상태입니다.");
                });

        accountStatusHistoryRepository.save(new AccountStatusHistory(account, newStatus));

        account.modifyStatus(newStatus);

        log.debug("[AdminService] 관리자 - {}: 계정 ({})의 상태를 {}(으)로 변경 성공", adminId, account.getLoginId(), newStatus);
    }

    /**
     * (관리자용) 특정 회원의 등급을 변경하는 메소드
     *
     * @param adminId Users 테이블 PK
     * @param userId  Users 테이블 PK
     * @param request 변경할 등급 이름
     */
    @Override
    @Transactional
    public void modifyUserGrade(Long adminId, Long userId, UserGradeRequest request) {
        User user = getUser(userId);

        Grade newGrade = gradeRepository.findByGradeName(request.gradeName())
                .orElseThrow(() -> {
                    log.error("[AdminService] 회원 등급 변경 실패: 존재하지 않는 등급 ({})", request.gradeName());
                    return new GradeNotFoundException("존재하지 않는 등급입니다.");
                });

        String reason = String.format("관리자 (%d) 수동 등급 변경", adminId);

        userGradeHistoryRepository.save(new UserGradeHistory(user, newGrade, reason));

        user.modifyGrade(newGrade);

        log.debug("[AdminService] 관리자 - {}: 회원 ({})의 등급을 {}(으)로 변경 성공", adminId, userId, newGrade);
    }

    private User getUser(Long userCreatedId) {
        return userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> {
                    log.warn("[AdminService] 찾을 수 없는 회원: {}", userCreatedId);
                    return new UserNotFoundException("찾을 수 없는 회원입니다.");
                });
    }

}
