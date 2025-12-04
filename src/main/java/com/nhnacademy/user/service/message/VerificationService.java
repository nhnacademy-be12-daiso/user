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

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.AccountStatusHistory;
import com.nhnacademy.user.exception.account.NotDormantAccountException;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import com.nhnacademy.user.repository.account.AccountStatusHistoryRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class VerificationService {  // 휴면 > 활성 전환을 위한 인증 처리 서비스 (MailService 기반)

    private final StringRedisTemplate redisTemplate;

    private final MailService mailService;

    private final AccountRepository accountRepository;

    private final AccountStatusHistoryRepository statusHistoryRepository;

    private static final String PREFIX = "ACTIVE_CODE:";

    private static final long LIMIT_TIME = 5 * 60;  // 5분

    @Transactional
    public void sendCode(Long userCreatedId) {  // 인증번호 발송
        log.info("[VerificationService] 휴면 해제 인증번호 발송 시작 - userCreatedId: {}", userCreatedId);

        try {
            Account account = accountRepository.findByUser_UserCreatedId(userCreatedId)
                    .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));
            log.info("[VerificationService] 계정 조회 성공 - userCreatedId: {}", userCreatedId);

            validateDormantAccount(account);
            log.info("[VerificationService] 휴면 상태 검증 성공");

            String email = account.getUser().getEmail();
            log.info("[VerificationService] 사용자 이메일: {}", email);

            try {
                log.info("[VerificationService] 메일 발송 시작 - email: {}", email);
                String code = mailService.sendCode(email);
                log.info("[VerificationService] 메일 발송 성공 - code: {}", code);

                // redis 저장: (key: ACTIVE_CODE:userCreatedId, value: 123456, TTL: 5분)
                redisTemplate.opsForValue().set(PREFIX + userCreatedId, code, LIMIT_TIME, TimeUnit.SECONDS);
                log.info("[VerificationService] Redis 저장 성공 - key: {}{}", PREFIX, userCreatedId);

                log.info("[VerificationService] 휴면 계정 활성화 인증번호 메일 발송 완료 - userCreatedId: {}, email: {}", userCreatedId,
                        email);

            } catch (Exception e) {
                log.error("[VerificationService] 메일 발송 중 에러 발생", e);
                throw new RuntimeException("인증코드 발송에 실패했습니다: " + e.getMessage(), e);
            }

        } catch (UserNotFoundException e) {
            log.error("[VerificationService] 사용자 조회 실패 - userCreatedId: {}", userCreatedId, e);
            throw e;

        } catch (NotDormantAccountException e) {
            log.error("[VerificationService] 휴면 상태 검증 실패 - userCreatedId: {}", userCreatedId, e);
            throw e;

        } catch (Exception e) {
            log.error("[VerificationService] 예상치 못한 에러 발생 - userCreatedId: {}", userCreatedId, e);
            throw new RuntimeException("인증코드 발송에 실패했습니다: " + e.getMessage(), e);
        }
    }

    public void verifyCode(Long userCreatedId, String code) {   // 인증 번호 검증
        String savedCode = redisTemplate.opsForValue().get(PREFIX + userCreatedId);

        if (savedCode == null || !savedCode.equals(code)) {
            log.warn("휴면 계정 인증 실패 - userCreatedId: {}", userCreatedId);

            throw new InvalidCodeException("올바르지 않은 코드입니다.");
        }

        // 인증 성공 시 redis에서 삭제 (재사용 방지)
        redisTemplate.delete(PREFIX + userCreatedId);

        log.info("휴면 계정 인증 성공 - userCreatedId: {}", userCreatedId);
    }

    public void validateDormantAccount(Account account) { // 계정의 상태 검증
        AccountStatusHistory latestHistory = statusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(account)
                .orElseThrow(() -> new RuntimeException("상태 정보가 없습니다."));

        if (!"DORMANT".equals(latestHistory.getStatus().getStatusName())) {
            throw new NotDormantAccountException("휴면 상태의 계정이 아닙니다.");
        }
    }

}
