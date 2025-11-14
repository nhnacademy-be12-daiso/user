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
import com.nhnacademy.user.dto.request.SignupRequest;
import com.nhnacademy.user.dto.response.UserResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.UserAlreadyExistsException;
import com.nhnacademy.user.exception.UserNotFoundException;
import com.nhnacademy.user.properties.JwtProperties;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.user.UserService;
import com.nhnacademy.user.util.JwtUtil;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
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

        Account account = accountRepository.findByIdWithUser(request.loginId())
                .orElseThrow(() -> new UserNotFoundException("인증은 성공했지만 찾을 수 없는 계정입니다.")); // 나오면 안 되는 에러

        // 최근 로그인 시간 업데이트
        account.getUser().modifyLastLoginAt();

        // JWT 토큰 생성(클라이언트에 반환할 access token)
        return jwtUtil.createAccessToken(account.getLoginId(), account.getRole().name());
    }

    @Override   // redis 저장만 수행하기 때문에 @Transactional 없음
    public void logout(String authHeader) { // 로그아웃
        // 토큰 추출
        String token = authHeader.substring(jwtProperties.getTokenPrefix().length() + 1);

        // 남은 유효 시간 확인
        long remainingExp = jwtUtil.getRemainingExpiration(token);

        // redis 블랙리스트에 등록
        // 로그아웃 토큰은 남은 유효 시간 동안 블랙리스트에 저장됨
        if (remainingExp > 0) {
            stringRedisTemplate.opsForValue()
                    .set(token, "logout", remainingExp, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String loginId) {
        Account account = accountRepository.findByIdWithUser(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("찾을 수 없는 계정입니다."));

        User user = account.getUser();

        return UserResponse.fromEntity(user);
    }

}
