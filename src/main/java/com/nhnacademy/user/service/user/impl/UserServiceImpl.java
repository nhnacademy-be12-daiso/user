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

import com.nhnacademy.user.dto.payco.PaycoLoginResponse;
import com.nhnacademy.user.dto.payco.PaycoSignUpRequest;
import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.BirthdayUserResponse;
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
import com.nhnacademy.user.exception.account.StateNotFoundException;
import com.nhnacademy.user.exception.user.GradeNotFoundException;
import com.nhnacademy.user.exception.user.PasswordNotMatchException;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.producer.CouponMessageProducer;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.account.StatusRepository;
import com.nhnacademy.user.repository.user.GradeRepository;
import com.nhnacademy.user.repository.user.UserGradeHistoryRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.point.PointService;
import com.nhnacademy.user.service.user.UserService;
import java.util.List;
import java.util.Optional;
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

    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    private final PointService pointService;

    private final PasswordEncoder passwordEncoder;

    private final CouponMessageProducer couponMessageProducer;

    private static final String WITHDRAWN_STATUS = "WITHDRAWN";

    @Override
    @Transactional  // user, account 둘 중 하나라도 저장 실패 시 롤백
    public void signUp(SignupRequest request) { // 회원가입
        if (accountRepository.existsById(request.loginId())) {
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
                .orElseThrow(() -> new GradeNotFoundException("시스템 오류: 초기 등급 데이터가 없습니다."));
        userGradeHistoryRepository.save(new UserGradeHistory(user, grade, "회원가입"));

        // 초기 상태(ACTIVE) 저장
        Status status = statusRepository.findByStatusName("ACTIVE")
                .orElseThrow(() -> new StateNotFoundException("시스템 오류: 초기 상태 데이터가 없습니다."));
        accountStatusHistoryRepository.save(new AccountStatusHistory(account, status));

        // 회원가입 축하 포인트 지급
        pointService.earnPointByPolicy(user.getUserCreatedId(), "REGISTER");

        log.info("회원가입 성공 - userCreatedId: {}, loginId: {}", saved.getUserCreatedId(), request.loginId());

        // 웰컴 쿠폰 발급 요청
        try {
            couponMessageProducer.sendWelcomeCouponMessage(saved.getUserCreatedId());
            // 웰컴 쿠폰 발급 요청(비동기 메시지 전송)
        } catch (Exception e) {
            log.error("웰컴 쿠폰 메시지 전송 실패 - userCreatedId: {}, error: {}", saved.getUserCreatedId(), e.getMessage());
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
                .orElseThrow(() -> new StateNotFoundException("계정 상태 정보가 누락되었습니다."));

        if (WITHDRAWN_STATUS.equals(status.getStatusName())) {
            throw new AccountWithdrawnException("이미 탈퇴한 계정입니다.");
        }

        Grade grade = userGradeHistoryRepository.findTopByUserOrderByChangedAtDesc(user)
                .map(UserGradeHistory::getGrade)
                .orElseThrow(() -> new GradeNotFoundException("회원 등급 정보가 누락되었습니다."));

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
        log.info("[회원정보수정] 시작 - userCreatedId: {}", userCreatedId);

        User user = getUser(userCreatedId);

        String currentPhone = user.getPhoneNumber();
        String currentEmail = user.getEmail();

        // Payco 더미 데이터인 경우 중복 검사 스킵 (더미 데이터에서 실제 데이터로 변경하는 경우)
        boolean isPhoneNumberDummy = currentPhone != null && currentPhone.startsWith("010-PAYCO-");
        boolean isEmailDummy = currentEmail != null && currentEmail.endsWith("@payco.user");

        // 전화번호 중복 검사 - 현재 전화번호와 다르고, 더미가 아닌 경우만 검사
        boolean phoneChanged = !request.phoneNumber().equals(currentPhone);
        if (phoneChanged) {
            // 더미 데이터이거나, 실제 데이터가 변경된 경우 중복 검사
            if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
                throw new UserAlreadyExistsException("이미 존재하는 연락처입니다.");
            }
        }

        // 이메일 중복 검사 - 현재 이메일과 다른 경우만 검사
        boolean emailChanged = !request.email().equals(currentEmail);
        if (emailChanged) {
            if (userRepository.existsByEmail(request.email())) {
                throw new UserAlreadyExistsException("이미 존재하는 이메일입니다.");
            }
        }

        user.modifyInfo(request.userName(), request.phoneNumber(), request.email(), request.birth());

        log.info("[회원정보수정] 완료 - userCreatedId: {}", userCreatedId);
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

        Status status = statusRepository.findByStatusName(WITHDRAWN_STATUS)
                .orElseThrow(() -> new StateNotFoundException("존재하지 않는 상태입니다."));

        // 계정 상태를 WITHDRAWN으로 변경
        accountStatusHistoryRepository.save(new AccountStatusHistory(account, status));

        log.info("회원 탈퇴 처리 완료 - userCreatedId: {}", userCreatedId);
        // 프론트에서 탈퇴 성공하면 브라우저가 가지고 있던 토큰을 스스로 삭제
    }

    @Override
    @Transactional(readOnly = true)
    public List<BirthdayUserResponse> findByBirthdayMonth(int month) {
        List<User> users = userRepository.findByBirthMonth(month);

        return users.stream()
                .map(user -> new BirthdayUserResponse(
                        user.getUserCreatedId(),
                        user.getUserName(),
                        user.getBirth()
                ))
                .toList();
    }

    @Override
    @Transactional
    public PaycoLoginResponse findOrCreatePaycoUser(PaycoSignUpRequest request) {
        String loginId = "PAYCO_" + request.getPaycoIdNo();

        Optional<Account> existingAccount = accountRepository.findById(loginId);

        if (existingAccount.isPresent()) {
            Account account = existingAccount.get();
            Status status = accountStatusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(account)
                    .map(AccountStatusHistory::getStatus)
                    .orElseThrow(() -> new StateNotFoundException("존재하지 않는 상태입니다."));
            if (WITHDRAWN_STATUS.equals(status.getStatusName())) {
                throw new UserNotFoundException("탈퇴한 계정입니다.");
            }

            // 프로필 완성 필요 여부 체크 (더미 데이터인 경우)
            User user = account.getUser();
            boolean needsProfileCompletion = isProfileIncomplete(user);

            log.info("[Payco] 기존 회원 로그인 - loginId: {}, needsProfileCompletion: {}", loginId, needsProfileCompletion);
            return new PaycoLoginResponse(
                    account.getUser().getUserCreatedId(),
                    account.getLoginId(),
                    account.getRole().name(),
                    false,
                    needsProfileCompletion
            );
        }

        // Payco에서 idNo만 제공받으므로 고유한 기본값 사용
        String userName = "Payco User";
        String uniqueId = request.getPaycoIdNo();
        String dummyEmail = "payco_" + uniqueId + "@payco.user";
        // 고유한 더미 전화번호 생성 (UNIQUE 제약 회피)
        String dummyPhone = "010-PAYCO-" + uniqueId.substring(0, Math.min(uniqueId.length(), 4));

        User newUser = new User(userName, dummyPhone, dummyEmail, null);
        userRepository.save(newUser);

        // Payco (OAuth) 사용자용 더미 비밀번호 생성 (로그인 불가능한 랜덤 값)
        String dummyPassword = passwordEncoder.encode("PAYCO_" + java.util.UUID.randomUUID());
        Account newAccount = new Account(loginId, dummyPassword, Role.USER, newUser);
        accountRepository.save(newAccount);

        Grade grade = gradeRepository.findByGradeName("GENERAL")
                .orElseThrow(() -> new GradeNotFoundException("시스템 오류: 초기 등급 데이터가 없습니다."));
        userGradeHistoryRepository.save(new UserGradeHistory(newUser, grade, "Payco 회원가입"));

        Status status = statusRepository.findByStatusName("ACTIVE")
                .orElseThrow(() -> new StateNotFoundException("시스템 오류: 초기 상태 데이터가 없습니다."));
        accountStatusHistoryRepository.save(new AccountStatusHistory(newAccount, status));

        pointService.earnPointByPolicy(newUser.getUserCreatedId(), "REGISTER");

        log.info("[Payco] 신규 회원 가입 - userCreatedId: {}, loginId: {}", newUser.getUserCreatedId(), loginId);

        try {
            couponMessageProducer.sendWelcomeCouponMessage(newUser.getUserCreatedId());

        } catch (Exception e) {
            log.error("웰컴 쿠폰 메시지 전송 실패 - userCreatedId: {}, error: {}", newUser.getUserCreatedId(), e.getMessage());
        }

        return new PaycoLoginResponse(
                newUser.getUserCreatedId(),
                newAccount.getLoginId(),
                newAccount.getRole().name(),
                true,
                true  // 신규 회원은 프로필 완성 필요
        );
    }

    /**
     * Payco 사용자의 프로필이 더미 데이터인지 확인
     */
    private boolean isProfileIncomplete(User user) {
        return "Payco User".equals(user.getUserName()) ||
                (user.getPhoneNumber() != null && user.getPhoneNumber().startsWith("010-PAYCO-")) ||
                (user.getEmail() != null && user.getEmail().endsWith("@payco.user"));
    }

    private User getUser(Long userCreatedId) {
        return userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));
    }

}