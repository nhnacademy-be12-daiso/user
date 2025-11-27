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
import com.nhnacademy.user.dto.response.PointResponse;
import com.nhnacademy.user.entity.point.Method;
import com.nhnacademy.user.entity.point.PointHistory;
import com.nhnacademy.user.entity.point.PointPolicy;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.point.PointNotEnoughException;
import com.nhnacademy.user.exception.point.PointPolicyNotFoundException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.point.PointHistoryRepository;
import com.nhnacademy.user.repository.point.PointPolicyRepository;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.service.point.PointService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class PointServiceImpl implements PointService {

    private final UserRepository userRepository;

    private final PointHistoryRepository pointHistoryRepository;

    private final PointPolicyRepository pointPolicyRepository;

    @Override
    @Transactional(readOnly = true)
    public PointResponse getCurrentPoint(Long userCreatedId) {  // 현재 내 포인트 잔액 조회
        User user = getUser(userCreatedId);

        BigDecimal point = pointHistoryRepository.getPointByUser(user);

        if (point == null) {
            point = BigDecimal.ZERO;
        }

        return new PointResponse(point);
    }

    @Override
    @Transactional
    public void earnPointByPolicy(Long userCreatedId, String policyType) {  // 정책 기반 포인트 적립
        earnPointByPolicy(userCreatedId, policyType, null);
    }

    @Override
    public void earnPointByPolicy(Long userCreatedId, String policyType, BigDecimal targetAmount) {
        User user = getUser(userCreatedId);

        PointPolicy pointPolicy = pointPolicyRepository.findByPolicyType(policyType)
                .orElseThrow(() -> new PointPolicyNotFoundException("존재하지 않는 포인트 정책입니다"));

        BigDecimal calculatedAmount;

        if (pointPolicy.getMethod() == Method.AMOUNT) {
            calculatedAmount = pointPolicy.getEarnPoint();  // 정책에 설정된 값 그대로 사용
        } else {
            if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("정률(RATIO) 정책은 기준 금액이 필수입니다.");
            }

            calculatedAmount = targetAmount.multiply(pointPolicy.getEarnPoint());
        }

        PointHistory pointHistory = new PointHistory(user, calculatedAmount, Type.EARN, pointPolicy.getPolicyName());

        pointHistoryRepository.save(pointHistory);

        log.info("정책 기반 포인트 적립 - userCreatedId: {}, type: {}, method: {}, amount: {}",
                userCreatedId, policyType, pointPolicy.getMethod(), calculatedAmount);
    }

    @Override
    @Transactional
    public void processPoint(PointRequest request) {    // 포인트 변동 수동 처리
        User user = userRepository.findByIdForUpdate(request.userCreatedId())
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));

        BigDecimal amount = request.amount();

        if (request.type() == Type.USE) {
            BigDecimal currentPoint = pointHistoryRepository.getPointByUser(user);

            if (currentPoint == null) {
                currentPoint = BigDecimal.ZERO;
            }

            if (currentPoint.compareTo(amount) < 0) {
                log.warn("포인트 사용 실패 (잔액 부족) - userCreatedId: {}, current: {}, request: {}",
                        request.userCreatedId(), currentPoint, amount);

                throw new PointNotEnoughException("포인트 잔액이 부족합니다. (현재: " + currentPoint + ")");
            }
        }

        PointHistory pointHistory = new PointHistory(user, amount, request.type(), request.description());

        pointHistoryRepository.save(pointHistory);

        log.info("포인트 변동 처리 - userCreatedId: {}, type: {}, amount: {}, description: {}",
                request.userCreatedId(), request.type(), request.amount(), request.description());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointHistoryResponse> getMyPointHistory(Long userCreatedId, Pageable pageable) {    // 내 포인트 내역 조회
        User user = getUser(userCreatedId);

        return pointHistoryRepository.findAllByUserOrderByCreatedAtDesc(user, pageable)
                .map(history -> new PointHistoryResponse(
                        history.getAmount(), history.getType(), history.getDescription(), history.getCreatedAt()));
    }

    private User getUser(Long userCreatedId) {
        return userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));
    }

}
