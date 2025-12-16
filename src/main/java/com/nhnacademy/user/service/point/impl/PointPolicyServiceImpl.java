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

import com.nhnacademy.user.dto.request.PointPolicyRequest;
import com.nhnacademy.user.dto.response.PointPolicyResponse;
import com.nhnacademy.user.entity.point.PointPolicy;
import com.nhnacademy.user.exception.point.PointPolicyAlreadyExistsException;
import com.nhnacademy.user.exception.point.PointPolicyNotFoundException;
import com.nhnacademy.user.repository.point.PointPolicyRepository;
import com.nhnacademy.user.service.point.PointPolicyService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class PointPolicyServiceImpl implements PointPolicyService {

    private final PointPolicyRepository pointPolicyRepository;

    private static final String CACHE_NAME = "pointPolicies";

    @Override
    @Transactional
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)  // 정책 추가 시 기존 캐시 삭제
    public void createPolicy(PointPolicyRequest request) {  // 포인트 정책 등록
        if (pointPolicyRepository.existsByPolicyType(request.policyType())) {
            log.warn("[포인트 정책] 추가 실패: 이미 존재하는 정책 ({})", request.policyType());
            throw new PointPolicyAlreadyExistsException("이미 존재하는 정책입니다.");
        }

        PointPolicy pointPolicy = new PointPolicy(
                request.policyName(), request.policyType(), request.method(), request.earnPoint());

        pointPolicyRepository.save(pointPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_NAME) // 정책 조회 시 캐시 적용
    public List<PointPolicyResponse> getPolicies() {    // 포인트 정책 전체 조회
        // 지금은 리스트로 받는데 포인트 정책이 많아질 거 같으면 추후에 페이징 처리 해야 할 듯
        return pointPolicyRepository.findAll().stream()
                .map(pointPolicy ->
                        new PointPolicyResponse(pointPolicy.getPointPolicyId(), pointPolicy.getPolicyName(),
                                pointPolicy.getPolicyType(), pointPolicy.getMethod(), pointPolicy.getEarnPoint()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true) // 정책 수정 시 기존 캐시 삭제
    public void modifyPolicy(Long pointPolicyId, PointPolicyRequest request) {  // 포인트 정책 수정
        PointPolicy pointPolicy = pointPolicyRepository.findById(pointPolicyId)
                .orElseThrow(() -> {
                    log.warn("[포인트 정책] 수정 실패: 찾을 수 없는 정책 ({})", pointPolicyId);
                    return new PointPolicyNotFoundException("찾을 수 없는 정책입니다.");
                });

        pointPolicy.modifyPolicy(request.policyName(), request.policyType(), request.method(), request.earnPoint());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true) // 정책 삭제 시 기존 캐시 삭제
    public void deletePolicy(Long pointPolicyId) {  // 포인트 정책 삭제
        if (!pointPolicyRepository.existsById(pointPolicyId)) {
            log.warn("[포인트 정책] 삭제 실패: 찾을 수 없는 정책 ({})", pointPolicyId);
            throw new PointPolicyNotFoundException("찾을 수 없는 정책입니다.");
        }

        pointPolicyRepository.deleteById(pointPolicyId);
    }

}
