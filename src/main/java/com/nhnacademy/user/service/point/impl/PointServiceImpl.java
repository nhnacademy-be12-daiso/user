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

package com.nhnacademy.user.service.point.impl;

import com.nhnacademy.user.dto.request.PointRequest;
import com.nhnacademy.user.dto.response.PointHistoryResponse;
import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.point.PointHistory;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.point.PointNotEnoughException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.point.PointHistoryRepository;
import com.nhnacademy.user.service.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PointServiceImpl implements PointService {

    private final AccountRepository accountRepository;

    private final PointHistoryRepository pointHistoryRepository;

    @Override
    @Transactional
    public void processPoint(PointRequest request) {
        User user = getAccount(request.loginId()).getUser();

        long changeAmount = request.amount();

        if (request.type() == Type.USE) {
            if (user.getPoint() < changeAmount) {
                throw new PointNotEnoughException("포인트 잔액이 부족합니다.");
            }

            changeAmount = -changeAmount;
        }

        user.modifyPoint(changeAmount);

        PointHistory pointHistory = new PointHistory(user, changeAmount, request.type(), request.description());

        pointHistoryRepository.save(pointHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointHistoryResponse> getMyPointHistory(String loginId, Pageable pageable) {
        Account account = getAccount(loginId);

        User user = account.getUser();

        return pointHistoryRepository.findAllByUserOrderByCreatedAtDesc(user, pageable)
                .map(PointHistoryResponse::fromEntity);
    }

    private Account getAccount(String loginId) {
        return accountRepository.findByIdWithUser(loginId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 계정입니다."));
    }

}
