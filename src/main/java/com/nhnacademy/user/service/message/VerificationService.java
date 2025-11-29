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

package com.nhnacademy.user.service.message;

import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.entity.user.UserStatusHistory;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.exception.user.NotDormantUserException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.user.UserRepository;
import com.nhnacademy.user.repository.user.UserStatusHistoryRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class VerificationService {  // 휴면 > 활성 전환을 위한 인증 처리 서비스 (Dooray Message Sender 기반)

    private final StringRedisTemplate redisTemplate;

    private final DoorayMessageSender doorayMessageSender;

    private final UserRepository userRepository;

    private final UserStatusHistoryRepository statusHistoryRepository;

    private static final String PREFIX = "ACTIVE_CODE:";

    private static final long LIMIT_TIME = 5 * 60;  // 5분

    @Transactional(readOnly = true)
    public void sendCode(Long userCreatedId) {  // 인증 번호 발송
        User user = userRepository.findByIdWithAccount(userCreatedId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));

        validateDormantAccount(user);

        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        // key: ACTIVE_CODE:loginId, value: 123456, TTL: 5분)
        redisTemplate.opsForValue().set(PREFIX + userCreatedId, code, LIMIT_TIME, TimeUnit.SECONDS);

        String loginId = user.getAccount().getLoginId();

        doorayMessageSender.send(loginId, "휴면 해제 인증번호 [" + code + "]를 입력해주세요.");

        log.info("휴면 해제 인증 코드 발송 요청 - userCreatedId: {}", userCreatedId);
    }

    public void verifyCode(Long userCreatedId, String code) {   // 인증 번호 검증
        String savedCode = redisTemplate.opsForValue().get(PREFIX + userCreatedId);

        if (savedCode == null || !savedCode.equals(code)) {
            log.warn("휴면 해제 인증 실패 - userCreatedId: {}", userCreatedId);

            throw new InvalidCodeException("올바르지 않은 코드입니다.");
        }

        // 인증 성공 시 redis에서 삭제 (재사용 방지)
        redisTemplate.delete(PREFIX + userCreatedId);

        log.info("휴면 해제 인증 성공 - userCreatedId: {}", userCreatedId);
    }

    public void validateDormantAccount(User user) { // 계정의 상태 검증
        UserStatusHistory latestHistory = statusHistoryRepository.findTopByUserOrderByChangedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("상태 정보가 없습니다."));

        if (!"DORMANT".equals(latestHistory.getStatus().getStatusName())) {
            throw new NotDormantUserException("휴면 상태의 회원이 아닙니다.");
        }
    }

}
