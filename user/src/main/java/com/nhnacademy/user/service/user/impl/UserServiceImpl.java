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

import com.nhnacademy.user.dto.request.LoginRequest;
import com.nhnacademy.user.dto.request.PasswordModifyRequest;
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.request.UserModifyRequest;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.user.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.user.UserAlreadyExistsException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.properties.JwtProperties;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.user.UserService;
import com.nhnacademy.user.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    // JWT 토큰 생성 및 검증
    private final JwtUtil jwtUtil;

    // JWT 설정값
    private final JwtProperties jwtProperties;

    // 로그아웃 토큰 블랙리스트 저장
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional  // user, account 둘 중 하나라도 저장 실패 시 롤백
    public void signUp(SignupRequest request) { // 회원가입
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new UserAlreadyExistsException("이미 존재하는 연락처입니다.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        if (accountRepository.existsByLoginId(request.loginId())) {
            throw new UserAlreadyExistsException("이미 존재하는 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(request.userName(), request.phoneNumber(), request.email(), request.birth());
        User saved = userRepository.save(user);

        Account account = new Account(request.loginId(), encodedPassword, Role.USER, saved);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public String login(LoginRequest request) { // 로그인
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.loginId(), request.password()));

        Account account = getAccount(request.loginId());

        // 최근 로그인 시간 업데이트
        account.getUser().modifyLastLoginAt();

        // JWT 토큰 생성(클라이언트에 반환할 access token)
        return jwtUtil.createAccessToken(account.getLoginId(), account.getRole().name());
    }

    @Override   // redis 저장만 수행하기 때문에 @Transactional 없음
    public void logout(String token) { // 로그아웃
        // 토큰 추출
        String jwt = token.substring(jwtProperties.getTokenPrefix().length() + 1);

        // 남은 유효 시간 확인
        long remainingExp = jwtUtil.getRemainingExpiration(jwt);

        // redis 블랙리스트에 등록
        // 로그아웃 토큰은 남은 유효 시간 동안 블랙리스트에 저장됨
        if (remainingExp > 0) {
            stringRedisTemplate.opsForValue()
                    .set(jwt, "logout", remainingExp, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String loginId) {   // 회원 정보 조회
        Account account = getAccount(loginId);

        User user = account.getUser();

        return UserResponse.fromEntity(user);
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
            throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }

        String newPassword = passwordEncoder.encode(request.newPassword());

        account.modifyPassword(newPassword);
    }

    @Override
    @Transactional
    public void withdrawUser(String loginId, String token) {    // 회원 탈퇴(회원 상태를 WITHDRAWN으로 바꿈)
        User user = getAccount(loginId).getUser();

        // 계정 상태를 WITHDRAWN으로 변경
        user.withdraw();

        // 현재 사용 중인 토큰도 즉시 무효화
        logout(token);
    }

    @Override
    @Transactional
    public void dormantAccounts() { // 휴면 계정 전환 배치 작업
        LocalDateTime lastLoginAtBefore = LocalDateTime.now().minusDays(90);

        // 휴면 대상자 조회
        List<User> dormantUsers = userRepository.findAllByStatusAndLastLoginAtBefore(Status.ACTIVE, lastLoginAtBefore);

        for (User dormantUser : dormantUsers) {
            dormantUser.dormant();
        }

        log.info("{}명의 회원을 휴면 계정으로 전환했습니다.", dormantUsers.size());
    }

    @Override
    public void activeUser(String loginId) {
        Account account = getAccount(loginId);

        User user = account.getUser();

        // 휴면 계정이 아니라면 실행할 필요 없음
        if (user.getStatus() != Status.DORMANT) {   // 휴면 계정 복구
            // 이미 활성 중이거나 탈퇴한 계정은 복구 대상이 아님
            throw new IllegalStateException("휴면 상태의 계정만 활성화할 수 있습니다.");
        }

        user.active();
    }

    private Account getAccount(String loginId) {
        return accountRepository.findByIdWithUser(loginId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 계정입니다."));
    }

}
