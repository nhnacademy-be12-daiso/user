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

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.account.StateNotFoundException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import com.nhnacademy.user.repository.account.StatusRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountStatusHistoryRepository accountStatusHistoryRepository;
    private final StatusRepository statusRepository;

    private static final String ACTIVE_STATUS = "ACTIVE";

    /**
     * 존재하는 계정 아이디인지 검증하는 메소드
     *
     * @param loginId 로그인 아이디
     * @return 로그인 아이디
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsLoginId(String loginId) {
        return accountRepository.existsById(loginId);
    }

    /**
     * 휴면 계정을 활성 계정으로 변환하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     */
    @Override
    @Transactional
    public void activeUser(Long userCreatedId) {
        User user = userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> {
                    log.warn("[AccountService] 계정 상태 변환 실패: 존재하지 않는 회원 ({})", userCreatedId);
                    return new UserNotFoundException("찾을 수 없는 회원입니다.");
                });

        Account account = user.getAccount();

        Status status = statusRepository.findByStatusName(ACTIVE_STATUS)
                .orElseThrow(() -> {
                    log.error("[AccountService] 계정 상태 변환 실패: 존재하지 않는 상태 ({})", ACTIVE_STATUS);
                    return new StateNotFoundException("존재하지 않는 상태입니다.");
                });

        accountStatusHistoryRepository.save(new AccountStatusHistory(account, status));
        accountRepository.save(account);
    }

}
