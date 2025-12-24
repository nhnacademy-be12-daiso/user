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
import com.nhnacademy.user.entity.point.Method;
import com.nhnacademy.user.entity.point.PointHistory;
import com.nhnacademy.user.entity.point.PointPolicy;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.exception.point.InvalidPointInputException;
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

    /**
     * 포인트 정책을 기반으로 포인트 적립하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @param policyType    포인트 정책 타입
     */
    @Override
    @Transactional
    public void earnPointByPolicy(Long userCreatedId, String policyType) {
        earnPointByPolicy(userCreatedId, policyType, null);
    }

    /**
     * 포인트 정책을 기반으로 포인트 적립하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @param policyType    포인트 정책 타입
     * @param targetAmount  적립할 포인트 값 (정액/정률)
     */
    @Override
    public void earnPointByPolicy(Long userCreatedId, String policyType, BigDecimal targetAmount) {
        User user = getLockUser(userCreatedId);

        PointPolicy pointPolicy = pointPolicyRepository.findByPolicyType(policyType)
                .orElseThrow(() -> {
                    log.error("[PointService] 포인트 정책 기반 적립 실패: 찾을 수 없는 포인트 정책 ({})", policyType);
                    return new PointPolicyNotFoundException("존재하지 않는 포인트 정책입니다.");
                });

        long calculatedAmount;

        if (pointPolicy.getMethod() == Method.AMOUNT) { // 정액일 때
            calculatedAmount = pointPolicy.getEarnPoint().longValue();  // 정책에 설정된 값 그대로 사용 (소수점 버림)

        } else {    // 정률일 때
            if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[PointService] 포인트 정책 기반 적립 실패: 잘못된 포인트 입력 값");
                throw new InvalidPointInputException("정률(RATIO) 정책은 기준 금액이 필수입니다.");
            }

            calculatedAmount = targetAmount.multiply(pointPolicy.getEarnPoint()).longValue();
        }

        // 포인트 내역 저장
        pointHistoryRepository.save(new PointHistory(user, calculatedAmount, Type.EARN, pointPolicy.getPolicyName()));

        // Users 테이블 현재 포인트 필드도 같이 동기화
        user.modifyPoint(calculatedAmount);
    }

    /**
     * 포인트 변동을 수동으로 처리하는 메소드
     *
     * @param request Users 테이블 PK, 적립할 금액, 포인트 타입(적립/사용/취소), 설명
     */
    @Override
    @Transactional
    public void processPoint(PointRequest request) {
        User user = getLockUser(request.userCreatedId());

        if (request.type() == Type.USE) {
            Long currentPoint = user.getCurrentPoint();

            if (currentPoint == null) {
                currentPoint = 0L;
            }

            if (currentPoint.compareTo(request.amount()) < 0) {
                log.warn("[PointService] 포인트 변동 수동 처리 실패: 잔액 부족 (현재: {})", currentPoint);
                throw new PointNotEnoughException("포인트 잔액이 부족합니다.");
            }
        }

        // 포인트 내역 저장
        pointHistoryRepository.save(new PointHistory(user, request.amount(), request.type(), request.description()));

        // Users 테이블 현재 포인트 필드도 같이 동기화
        user.modifyPoint(request.amount());
    }

    /**
     * 포인트 내역 조회하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @param pageable      페이징 처리
     * @return 페이징된 전체 포인트 내역 목록
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PointHistoryResponse> getMyPointHistory(Long userCreatedId, Pageable pageable) {
        User user = getUser(userCreatedId);

        return pointHistoryRepository.findAllByUserOrderByCreatedAtDesc(user, pageable)
                .map(history -> new PointHistoryResponse(
                        history.getAmount(), history.getType(), history.getDescription(), history.getCreatedAt()));
    }

    private User getUser(Long userCreatedId) {
        return userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));
    }

    private User getLockUser(Long userCreatedId) {
        return userRepository.findByIdForUpdate(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("찾을 수 없는 회원입니다."));
    }

}
