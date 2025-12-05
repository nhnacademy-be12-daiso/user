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

    private final StatusRepository statusRepository;

    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean existsLoginId(String loginId) {
        return accountRepository.existsById(loginId);
    }

    @Override
    @Transactional
    public void activeUser(Long userCreatedId) {
        User user = userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));

        Account account = user.getAccount();

        Status status = getStatus("ACTIVE");

        accountStatusHistoryRepository.save(new AccountStatusHistory(account, status));

        log.info("휴면 계정 활성화 완료 - userCreatedId: {}", userCreatedId);
    }

    private Status getStatus(String statusName) {
        return statusRepository.findByStatusName(statusName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상태입니다."));
    }

}
