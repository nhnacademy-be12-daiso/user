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
import com.nhnacademy.user.exception.message.MailSendException;
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

    /**
     * 회원의 이름이랑 회원의 이메일로 계정 아이디를 찾는 메소드
     *
     * @param request 회원 이름과 회원 이메일
     * @return 마스킹된 계정 아이디
     */
    @Override
    @Transactional(readOnly = true)
    public String findLoginId(FindLoginIdRequest request) {
        User user = userRepository.findByUserNameAndEmail(request.userName(), request.email())
                .orElseThrow(() -> {
                    log.warn("[FindAccountService] 아이디 찾기 실패: 존재하지 않는 이름 - 이메일 조합");
                    return new UserNotFoundException("찾을 수 없는 회원입니다.");
                });

        return MaskingUtils.maskLoginId(user.getAccount().getLoginId());
    }

    /**
     * 계정 아이디랑 회원 이름, 회원 이메일로 임시 비밀번호를 발급하는 메소드
     *
     * @param request 계정 아이디와 회원 이름, 회원 이메일
     */
    @Override
    public void createTemporaryPassword(FindPasswordRequest request) {  // 로그인 아이디랑 이름, 이메일로 비밀번호 찾기 (임시 비밀번호 발급)
        Account account = accountRepository.findById(request.loginId())
                .orElseThrow(() -> {
                    log.warn("[FindAccountService] 임시 비밀번호 발급 실패: 존재하지 않는 계장 ({})", request.loginId());
                    return new UserNotFoundException("찾을 수 없는 계정입니다.");
                });

        User user = account.getUser();

        if (!user.getUserName().equals(request.userName()) || !user.getEmail().equals(request.email())) {
            log.warn("[FindAccountService] 임시 비밀번호 발급 실패: 회원 정보와 일치하지 않는 입력 값");
            throw new UserNotFoundException("입력하신 정보가 회원 정보와 일치하지 않습니다.");
        }

        String temporaryPassword = RandomPasswordUtils.createTemporaryPassword();

        account.modifyPassword(passwordEncoder.encode(temporaryPassword));

        try {
            mailService.sendTemporaryPassword(user.getEmail(), temporaryPassword);

        } catch (Exception e) {
            log.error("[FindAccountService] 임시 비밀번호 발급 실패: 메일 전송 실패");
            throw new MailSendException("메일 발송 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }

}
