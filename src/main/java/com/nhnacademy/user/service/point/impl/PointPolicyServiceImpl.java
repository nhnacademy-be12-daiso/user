/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2026. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.user.service.point.impl;

import com.nhnacademy.user.dto.request.PointPolicyRequest;
import com.nhnacademy.user.dto.response.PointPolicyResponse;
import com.nhnacademy.user.entity.point.PointPolicy;
import com.nhnacademy.user.exception.point.PointPolicyAlreadyExistsException;
import com.nhnacademy.user.exception.point.PointPolicyNotFoundException;
import com.nhnacademy.user.repository.point.PointPolicyRepository;
import com.nhnacademy.user.service.point.PointPolicyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class PointPolicyServiceImpl implements PointPolicyService {

    private final PointPolicyRepository pointPolicyRepository;

    /**
     * (관리자 전용) 포인트 정책을 등록하는 메소드
     *
     * @param request 정책 이름, 정책 타입, 적립 방식, 적립 값
     */
    @Override
    @Transactional
    public void createPolicy(PointPolicyRequest request) {
        if (pointPolicyRepository.existsByPolicyType(request.policyType())) {
            log.warn("[PointPolicyService] 포인트 정책 추가 실패: 이미 존재하는 정책 ({})", request.policyType());
            throw new PointPolicyAlreadyExistsException("이미 존재하는 정책입니다.");
        }

        pointPolicyRepository.save(
                new PointPolicy(request.policyName(), request.policyType(), request.method(), request.earnPoint()));
    }

    /**
     * (관리자 전용) 포인트 정책을 조회하는 메소드 - 추후 페이징 처리 고려
     *
     * @return 포인트 정책 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<PointPolicyResponse> getPolicies() {
        return pointPolicyRepository.findAll().stream()
                .map(pointPolicy ->
                        new PointPolicyResponse(pointPolicy.getPointPolicyId(),
                                pointPolicy.getPolicyName(),
                                pointPolicy.getPolicyType(),
                                pointPolicy.getMethod(),
                                pointPolicy.getEarnPoint()))
                .toList();
    }

    /**
     * (관리자 전용) 포인트 정책을 수정하는 메소드
     *
     * @param pointPolicyId PointPolicies 테이블 PK
     * @param request       정책 이름, 정책 타입, 적립 방식, 적립 값
     */
    @Override
    @Transactional
    public void modifyPolicy(Long pointPolicyId, PointPolicyRequest request) {
        PointPolicy pointPolicy = pointPolicyRepository.findById(pointPolicyId)
                .orElseThrow(() -> {
                    log.warn("[PointPolicyService] 포인트 정책 수정 실패: 찾을 수 없는 정책 ({})", pointPolicyId);
                    return new PointPolicyNotFoundException("찾을 수 없는 정책입니다.");
                });

        pointPolicy.modifyPolicy(request.policyName(), request.policyType(), request.method(), request.earnPoint());
    }

    /**
     * (관리자 전용) 포인트 정책을 삭제하는 메소드
     *
     * @param pointPolicyId PointPolicies 테이블 PK
     */
    @Override
    @Transactional
    public void deletePolicy(Long pointPolicyId) {
        if (!pointPolicyRepository.existsById(pointPolicyId)) {
            log.warn("[PointPolicyService] 포인트 정책 삭제 실패: 찾을 수 없는 정책 ({})", pointPolicyId);
            throw new PointPolicyNotFoundException("찾을 수 없는 정책입니다.");
        }

        pointPolicyRepository.deleteById(pointPolicyId);
    }

}
