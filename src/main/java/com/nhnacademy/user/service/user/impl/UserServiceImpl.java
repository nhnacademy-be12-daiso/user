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

import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.BirthdayUserDto;
import com.nhnacademy.user.dto.response.InternalAddressResponse;
import com.nhnacademy.user.dto.response.InternalUserResponse;
import com.nhnacademy.user.dto.response.PointResponse;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import com.nhnacademy.user.exception.account.AccountWithdrawnException;
import com.nhnacademy.user.exception.user.PasswordNotMatchException;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.producer.CouponMessageProducer;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.account.StatusRepository;
import com.nhnacademy.user.repository.address.AddressRepository;
import com.nhnacademy.user.repository.user.GradeRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.point.PointService;
import com.nhnacademy.user.service.user.UserService;
import java.math.BigDecimal;
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

    private final AddressRepository addressRepository;

    private final GradeRepository gradeRepository;

    private final StatusRepository statusRepository;

    private final UserGradeHistoryRepository userGradeHistoryRepository;

    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    private final PointService pointService;

    private final PasswordEncoder passwordEncoder;

    private final CouponMessageProducer couponMessageProducer; // rabbitMq 방식으로 보낼거라 바꿈.

    @Override
    @Transactional(readOnly = true)
    public boolean existsUser(Long userCreatedId) { // 회원 유효성 검증
        return userRepository.existsById(userCreatedId);
    }

    @Override
    @Transactional(readOnly = true)
    public InternalUserResponse getInternalUserInfo(Long userCreatedId) {   // 주문/결제용 회원 정보 조회
        User user = getUser(userCreatedId);
        Account account = user.getAccount();

        Status status = accountStatusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(account)
                .map(AccountStatusHistory::getStatus)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상태입니다."));

        if ("WITHDRAWN".equals(status.getStatusName())) {
            throw new UserNotFoundException("탈퇴한 계정입니다.");
        }

        String grade = userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(user)
                .map(history -> history.getGrade().getGradeName())
                .orElse("GENERAL");

        BigDecimal point = pointService.getCurrentPoint(userCreatedId).currentPoint();

        InternalAddressResponse addressResponse = addressRepository.findFirstByUserAndIsDefaultTrue(user)
                .map(address -> new InternalAddressResponse(
                        address.getAddressName(), address.getRoadAddress(), address.getAddressDetail()))
                .orElse(null);

        return new InternalUserResponse(userCreatedId,
                user.getUserName(), user.getPhoneNumber(), user.getEmail(), grade, point, addressResponse);
    }

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
        accountStatusHistoryRepository.save(new AccountStatusHistory(account, status));

        // 회원가입 축하 포인트 지급
        pointService.earnPointByPolicy(user.getUserCreatedId(), "REGISTER");

        log.info("회원가입 성공 - userCreatedId: {}, loginId: {}", saved.getUserCreatedId(), request.loginId());

        // 웰컴 쿠폰 발급 요청
        try {
            couponMessageProducer.sendWelcomeCouponMessage(saved.getUserCreatedId());
            // 웰컴 쿠폰 발급 요청(비동기 메시지 전송)
        } catch (Exception e) {
            log.error("웰컴 쿠폰 메시지 전송 실패 (나중에 재발급 배치 필요): {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(Long userCreatedId) {   // 회원 정보 조회
        log.info("회원 정보 조회 시작 - userCreatedId: {}", userCreatedId);

        User user = getUser(userCreatedId);
        Account account = user.getAccount();

        Status status = accountStatusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(account)
                .map(AccountStatusHistory::getStatus)
                .orElseThrow(() -> new RuntimeException("계정 상태 정보가 누락되었습니다."));

        if ("WITHDRAWN".equals(status.getStatusName())) {
            throw new AccountWithdrawnException("이미 탈퇴한 계정입니다.");
        }

        Grade grade = userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(user)
                .map(UserGradeHistory::getGrade)
                .orElseThrow(() -> new RuntimeException("회원 등급 정보가 누락되었습니다."));

        PointResponse pointResponse = pointService.getCurrentPoint(userCreatedId);

        log.info("회원 정보 조회 완료 - userCreatedId: {}, loginId: {}, status: {}",
                userCreatedId, user.getAccount().getLoginId(), status.getStatusName());

        return new UserResponse(userCreatedId, account.getLoginId(),
                user.getUserName(), user.getPhoneNumber(), user.getEmail(), user.getBirth(),
                grade.getGradeName(), pointResponse.currentPoint(), status.getStatusName(), account.getJoinedAt());
    }

    @Override
    @Transactional
    public void modifyUserInfo(Long userCreatedId, UserModifyRequest request) { // 회원 정보 수정
        User user = getUser(userCreatedId);

        user.modifyInfo(request.userName(), request.phoneNumber(), request.email(), request.birth());
    }

    @Override
    @Transactional
    public void modifyUserPassword(Long userCreatedId, PasswordModifyRequest request) { // 비밀번호 수정
        User user = getUser(userCreatedId);
        Account account = user.getAccount();

        if (!passwordEncoder.matches(request.currentPassword(), account.getPassword())) {
            throw new PasswordNotMatchException("현재 비밀번호가 일치하지 않습니다.");
        }

        account.modifyPassword(passwordEncoder.encode(request.newPassword()));
    }

    @Override
    @Transactional
    public void withdrawUser(Long userCreatedId) {    // 회원 탈퇴(회원 상태를 WITHDRAWN으로 바꿈)
        User user = getUser(userCreatedId);
        Account account = user.getAccount();

        Status status = statusRepository.findByStatusName("WITHDRAWN")
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상태입니다."));

        // 계정 상태를 WITHDRAWN으로 변경
        accountStatusHistoryRepository.save(new AccountStatusHistory(account, status));

        log.info("회원 탈퇴 처리 완료 - userCreatedId: {}", userCreatedId);
        // 프론트에서 탈퇴 성공하면 브라우저가 가지고 있던 토큰을 스스로 삭제
    }

    private User getUser(Long userCreatedId) {
        return userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));
    }

    @Override
    public List<BirthdayUserDto> findByBirthdayMonth(int month) {
        List<User> users = userRepository.findByBirthMonth(month);

        return users.stream()
                .map(user -> new BirthdayUserDto(
                        user.getUserCreatedId(),
                        user.getUserName(),
                        user.getBirth()
                ))
                .toList();
    }
}
