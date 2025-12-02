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

package com.nhnacademy.user.service.account.impl;

import com.nhnacademy.user.common.MaskingUtils;
import com.nhnacademy.user.common.RandomPasswordUtils;
import com.nhnacademy.user.dto.request.FindLoginIdRequest;
import com.nhnacademy.user.dto.request.FindPasswordRequest;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.account.FindAccountService;
import com.nhnacademy.user.service.message.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class FindAccountServiceImpl implements FindAccountService {

    private final UserRepository userRepository;

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

    @Override
    @Transactional(readOnly = true)
    public String findLoginId(FindLoginIdRequest request) { // 이름이랑 이메일로 아이디 찾기
        User user = userRepository.findByUserNameAndEmail(request.userName(), request.email())
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));

        String loginId = user.getAccount().getLoginId();

        log.info("아이디 찾기 성공 - userCreatedId: {}, loginId: {}", user.getUserCreatedId(), loginId);

        return MaskingUtils.maskLoginId(loginId);
    }

    @Override
    @Transactional
    public void createTemporaryPassword(FindPasswordRequest request) {  // 로그인 아이디랑 이름, 이메일로 비밀번호 찾기 (임시 비밀번호 발급)
        Account account = accountRepository.findById(request.loginId())
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 계정입니다."));

        User user = account.getUser();

        if (!user.getUserName().equals(request.userName()) || !user.getEmail().equals(request.email())) {
            throw new UserNotFoundException("입력하신 정보가 회원 정보와 일치하지 않습니다.");
        }

        String temporaryPassword = RandomPasswordUtils.createTemporaryPassword();

        account.modifyPassword(passwordEncoder.encode(temporaryPassword));

        try {
            mailService.sendTemporaryPassword(user.getEmail(), temporaryPassword);

            log.info("임시 비밀번호 발급 성공 - loginId: {}, email: {}", request.loginId(), request.email());

        } catch (Exception e) {
            log.info("임시 비밀번호 발급 메일 전송 실패 - loginId: {}", request.loginId());

            throw new RuntimeException("메일 발송 중 오류가 발생했습니다. 다시 시도해주세요.", e);
        }
    }

}
