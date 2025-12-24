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
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserGradeHistory;
import com.nhnacademy.user.event.WelcomeCouponEvent;
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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final UserGradeHistoryRepository userGradeHistoryRepository;
    private final AccountRepository accountRepository;
    private final StatusRepository statusRepository;
    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    private final PointService pointService;

    private final PasswordEncoder passwordEncoder;

    private final CouponMessageProducer couponMessageProducer;

    private final ApplicationEventPublisher eventPublisher;

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String WITHDRAWN_STATUS = "WITHDRAWN";
    private static final String GENERAL_GRADE = "GENERAL";
    private static final String SIGNUP_POINT_POLICY_TYPE = "REGISTER";

    private static final String PAYCO_PHONE_NUMBER_PREFIX = "010-PAYCO-";
    private static final String PAYCO_EMAIL_SUFFIX = "@payco.user";

    private static final String CACHE_NAME = "users";

    /**
     * 회원가입하는 메소드
     *
     * @param request 로그인 아이디, 비밀번호, 이름, 연락처, 이메일, 생일
     */
    @Override
    @Transactional  // user, account 둘 중 하나라도 저장 실패 시 롤백
    public void signUp(SignupRequest request) {
        if (accountRepository.existsById(request.loginId())) {
            log.warn("[UserService] 회원가입 실패: 로그인 아이디 중복");
            throw new UserAlreadyExistsException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            log.warn("[UserService] 회원가입 실패: 연락처 중복");
            throw new UserAlreadyExistsException("이미 존재하는 연락처입니다.");
        }

        if (userRepository.existsByEmail(request.email())) {
            log.warn("[UserService] 회원가입 실패: 이메일 중복");
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
        Grade grade = gradeRepository.findByGradeName(GENERAL_GRADE)
                .orElseThrow(() -> {
                    log.error("[UserService] 회원가입 실패: 존재하지 않는 등급 ({})", GENERAL_GRADE);
                    return new GradeNotFoundException("시스템 오류: 초기 등급 데이터가 없습니다.");
                });
        userGradeHistoryRepository.save(new UserGradeHistory(user, grade, "회원가입"));
        user.modifyGrade(grade);

        // 초기 상태(ACTIVE) 저장
        Status status = statusRepository.findByStatusName(ACTIVE_STATUS)
                .orElseThrow(() -> {
                    log.error("[UserService] 회원가입 실패: 존재하지 않는 상태 ({})", ACTIVE_STATUS);
                    return new StateNotFoundException("시스템 오류: 초기 상태 데이터가 없습니다.");
                });
        accountStatusHistoryRepository.save(new AccountStatusHistory(account, status));
        account.modifyStatus(status);

        // 회원가입 축하 포인트 지급
        pointService.earnPointByPolicy(user.getUserCreatedId(), SIGNUP_POINT_POLICY_TYPE);

        // 웰컴 쿠폰 발급 요청
        eventPublisher.publishEvent(new WelcomeCouponEvent(saved.getUserCreatedId()));
    }

    /**
     * 회원 정보를 조회하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @return Users 테이블 PK, 회원 정보 (이름, 연락처, 이메일, 생일, 등급, 보유 포인트), 계정 정보 (로그인 아이디, 상태, 가입일)
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(Long userCreatedId) {
        User user = getUser(userCreatedId);

        if (WITHDRAWN_STATUS.equals(user.getAccount().getStatus().getStatusName())) {
            log.warn("[UserService] 마이페이지 조회 실패: 탈퇴한 계정");
            throw new AccountWithdrawnException("이미 탈퇴한 계정입니다.");
        }

        return new UserResponse(userCreatedId,
                user.getAccount().getLoginId(),
                user.getUserName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getBirth(),
                user.getGrade().getGradeName(),
                user.getCurrentPoint(),
                user.getAccount().getStatus().getStatusName(),
                user.getAccount().getJoinedAt());
    }

    /**
     * 회원 정보를 수정하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @param request       이름, 연락처, 이메일, 생일
     */
    @Override
    @Transactional
    public void modifyUserInfo(Long userCreatedId, UserModifyRequest request) { // 회원 정보 수정
        User user = getUser(userCreatedId);

        String currentPhone = user.getPhoneNumber();
        String currentEmail = user.getEmail();

        // 전화번호 중복 검사 - 현재 전화번호와 다른 경우만 검사
        if (!request.phoneNumber().equals(currentPhone) && userRepository.existsByPhoneNumber(request.phoneNumber())) {
            log.warn("[UserService] 마이페이지 수정 실패: 연락처 중복");
            throw new UserAlreadyExistsException("이미 존재하는 연락처입니다.");
        }

        // 이메일 중복 검사 - 현재 이메일과 다른 경우만 검사
        if (!request.email().equals(currentEmail) && userRepository.existsByEmail(request.email())) {
            log.warn("[UserService] 마이페이지 수정 실패: 이메일 중복");
            throw new UserAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        user.modifyInfo(request.userName(), request.phoneNumber(), request.email(), request.birth());
    }

    /**
     * 계정 비밀번호를 변경하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @param request       현재 비밀번호, 변경할 비밀번호
     */
    @Override
    @Transactional
    public void modifyAccountPassword(Long userCreatedId, PasswordModifyRequest request) {
        User user = getUser(userCreatedId);

        Account account = user.getAccount();

        if (!passwordEncoder.matches(request.currentPassword(), account.getPassword())) {
            log.warn("[UserService] 마이페이지 비밀번호 수정 실패: 일치하지 않는 비밀번호");
            throw new PasswordNotMatchException("현재 비밀번호가 일치하지 않습니다.");
        }

        account.modifyPassword(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * 회원 탈퇴하는 메소드 - 회원 상태를 탈퇴 (WITHDRAWN)으로 변경
     *
     * @param userCreatedId Users 테이블 PK
     */
    @Override
    @Transactional
    public void withdrawUser(Long userCreatedId) {
        User user = getUser(userCreatedId);

        Account account = user.getAccount();

        Status status = statusRepository.findByStatusName(WITHDRAWN_STATUS)
                .orElseThrow(() -> {
                    log.error("[UserService] 마이페이지 탈퇴 실패: 존재하지 않는 상태 ({})", WITHDRAWN_STATUS);
                    return new StateNotFoundException("시스템 오류: 초기 상태 데이터가 없습니다.");
                });
        accountStatusHistoryRepository.save(new AccountStatusHistory(account, status));
        account.modifyStatus(status);

        // 프론트에서 탈퇴 성공하면 브라우저가 가지고 있던 토큰을 스스로 삭제
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<BirthdayUserResponse> findByBirthdayMonth(int month, Pageable pageable) {
        Slice<User> users = userRepository.findByBirthMonth(month, pageable);

        return users.map(user ->
                new BirthdayUserResponse(
                        user.getUserCreatedId(),
                        user.getUserName(),
                        user.getBirth()));
    }

    @Override
    @Transactional
    public PaycoLoginResponse findOrCreatePaycoUser(PaycoSignUpRequest request) {
        String loginId = "PAYCO_" + request.paycoIdNo();

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
        String uniqueId = request.paycoIdNo();
        String dummyEmail = "payco_" + uniqueId + PAYCO_EMAIL_SUFFIX;
        // 고유한 더미 전화번호 생성 (UNIQUE 제약 회피)
        String dummyPhone = PAYCO_PHONE_NUMBER_PREFIX + uniqueId.substring(0, Math.min(uniqueId.length(), 4));

        User newUser = new User(userName, dummyPhone, dummyEmail, null);
        userRepository.save(newUser);

        // Payco (OAuth) 사용자용 더미 비밀번호 생성 (로그인 불가능한 랜덤 값)
        String dummyPassword = passwordEncoder.encode("PAYCO_" + java.util.UUID.randomUUID());
        Account newAccount = new Account(loginId, dummyPassword, Role.USER, newUser);
        accountRepository.save(newAccount);

        Grade grade = gradeRepository.findByGradeName("GENERAL")
                .orElseThrow(() -> new GradeNotFoundException("시스템 오류: 초기 등급 데이터가 없습니다."));
        userGradeHistoryRepository.save(new UserGradeHistory(newUser, grade, "Payco 회원가입"));
        newUser.modifyGrade(grade);

        Status status = statusRepository.findByStatusName("ACTIVE")
                .orElseThrow(() -> new StateNotFoundException("시스템 오류: 초기 상태 데이터가 없습니다."));
        accountStatusHistoryRepository.save(new AccountStatusHistory(newAccount, status));
        newAccount.modifyStatus(status);

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
                (user.getPhoneNumber() != null && user.getPhoneNumber().startsWith(PAYCO_PHONE_NUMBER_PREFIX)) ||
                (user.getEmail() != null && user.getEmail().endsWith(PAYCO_EMAIL_SUFFIX));
    }

    private User getUser(Long userCreatedId) {
        return userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));
    }

}