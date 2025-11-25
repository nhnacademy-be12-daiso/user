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

import com.nhnacademy.user.adapter.CouponFeignClient;
import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.PointResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import com.nhnacademy.user.entity.user.UserStatusHistory;
import com.nhnacademy.user.exception.user.PasswordNotMatchException;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.exception.user.UserWithdrawnException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.user.GradeRepository;
import com.nhnacademy.user.repository.user.StatusRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.repository.user.UserStatusHistoryRepository;
import com.nhnacademy.user.service.point.PointService;
import com.nhnacademy.user.service.user.UserService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final AccountRepository accountRepository;

    private final GradeRepository gradeRepository;

    private final StatusRepository statusRepository;

    private final UserGradeHistoryRepository userGradeHistoryRepository;

    private final UserStatusHistoryRepository userStatusHistoryRepository;

    private final PointService pointService;

    private final PasswordEncoder passwordEncoder;

    private final CouponFeignClient couponFeignClient;

    @Override
    @Transactional  // user, account 둘 중 하나라도 저장 실패 시 롤백
    public void signUp(SignupRequest request) { // 회원가입
        if (accountRepository.existsByLoginId(request.loginId())) {
            throw new UserAlreadyExistsException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new UserAlreadyExistsException("이미 존재하는 연락처입니다.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        // Users 테이블 저장
        User user = new User(request.userName(), request.phoneNumber(), request.email(), request.birth());
        User saved = userRepository.save(user);

        String encodedPassword = passwordEncoder.encode(request.password());

        // Accounts 테이블 저장 (인코딩된 비밀번호)
        Account account = new Account(request.loginId(), encodedPassword, Role.USER, user);
        accountRepository.save(account);

        // 초기 등급(GENERAL) 저장
        Grade grade = gradeRepository.findByGradeName("GENERAL")
                .orElseThrow(() -> new RuntimeException("시스템 오류: 초기 등급 데이터가 없습니다."));
        userGradeHistoryRepository.save(new UserGradeHistory(user, grade, "회원가입"));

        // 초기 상태(ACTIVE) 저장
        Status status = statusRepository.findByStatusName("ACTIVE")
                .orElseThrow(() -> new RuntimeException("시스템 오류: 초기 상태 데이터가 없습니다."));
        userStatusHistoryRepository.save(new UserStatusHistory(user, status));

        // 회원가입 축하 포인트 지급
        pointService.earnPointByPolicy(request.loginId(), "REGISTER");

        // 웰컴 쿠폰 발급 요청
        try {
            couponFeignClient.issueWelcomeCoupon(saved.getUserCreatedId());
            log.info("웰컴 쿠폰 발급 요청 성공: userId={}", saved.getUserCreatedId());
        } catch (Exception e) {
            log.error("웰컴 쿠폰 발급 실패 (가입은 정상 처리됨): userId={}, error={}", saved.getUserCreatedId(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String loginId) {   // 회원 정보 조회
        Account account = getAccount(loginId);

        User user = account.getUser();

        Status status = userStatusHistoryRepository.findTopByUserOrderByChangedAtDesc(user)
                .map(UserStatusHistory::getStatus)
                .orElseThrow(() -> new RuntimeException("회원 상태 정보가 누락되었습니다."));

        if ("WITHDRAWN".equals(status.getStatusName())) {
            throw new UserWithdrawnException("이미 탈퇴한 회원입니다.");
        }

        Grade grade = userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(user)
                .map(UserGradeHistory::getGrade)
                .orElseThrow(() -> new RuntimeException("회원 등급 정보가 누락되었습니다."));

        PointResponse pointResponse = pointService.getCurrentPoint(loginId);

        return new UserResponse(user.getUserName(), user.getPhoneNumber(), user.getEmail(), user.getBirth(),
                grade.getGradeName(), pointResponse.currentPoint(), status.getStatusName(), user.getJoinedAt());
    }

    @Override
    @Transactional
    public void modifyUserInfo(String loginId, UserModifyRequest request) { // 회원 정보 수정
        User user = getAccount(loginId).getUser();

        user.modifyInfo(request.userName(), request.phoneNumber(), request.email(), request.birth());
    }

    @Override
    @Transactional
    public void modifyUserPassword(String loginId, PasswordModifyRequest request) { // 비밀번호 수정
        Account account = getAccount(loginId);

        if (!passwordEncoder.matches(request.currentPassword(), account.getPassword())) {
            throw new PasswordNotMatchException("현재 비밀번호가 일치하지 않습니다.");
        }

        account.modifyPassword(passwordEncoder.encode(request.newPassword()));
    }

    @Override
    public void modifyLastLoginAt(String loginId) {
        Account account = getAccount(loginId);

        account.getUser().modifyLastLoginAt();
    }

    @Override
    @Transactional
    public void withdrawUser(String loginId) {    // 회원 탈퇴(회원 상태를 WITHDRAWN으로 바꿈)
        // 계정 상태를 WITHDRAWN으로 변경
        changeStatus(loginId, "WITHDRAWN");

        // 프론트에서 탈퇴 성공하면 브라우저가 가지고 있던 토큰을 스스로 삭제
    }

    @Override
    @Transactional
    public void dormantAccounts() { // 휴면 계정 전환 배치 작업
        LocalDateTime lastLoginAtBefore = LocalDateTime.now().minusDays(90);

        // 휴면 대상자 조회
        List<User> dormantUsers = userRepository.findDormantUser(lastLoginAtBefore);

        Status status = statusRepository.findByStatusName("DORMANT")
                .orElseThrow(() -> new RuntimeException("DORMANT 상태 없음"));

        for (User dormantUser : dormantUsers) {
            userStatusHistoryRepository.save(new UserStatusHistory(dormantUser, status));
        }

        log.info("{}명의 회원을 휴면 계정으로 전환했습니다.", dormantUsers.size());
    }

    @Override
    public void activeUser(String loginId) {
        // 휴면 해제 시 인증 절차 필요함!! > dooray messenger sender..??
        changeStatus(loginId, "ACTIVE");
    }

    private Account getAccount(String loginId) {
        return accountRepository.findByIdWithUser(loginId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 계정입니다."));
    }

    private void changeStatus(String loginId, String statusName) {
        User user = getAccount(loginId).getUser();

        Status status = statusRepository.findByStatusName(statusName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상태: " + statusName));

        userStatusHistoryRepository.save(new UserStatusHistory(user, status));
    }

}
