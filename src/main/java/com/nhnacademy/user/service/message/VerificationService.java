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
import com.nhnacademy.user.exception.account.StateNotFoundException;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.exception.message.MailSendException;
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

    private final AccountRepository accountRepository;
    private final AccountStatusHistoryRepository accountStatusHistoryRepository;

    private final MailService mailService;

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "DORMANT_RELEASE_CODE:";

    private static final long LIMIT_TIME = (long) 5 * 60;  // 5분

    @Transactional(readOnly = true)
    public void sendCode(Long userCreatedId) {  // 인증번호 발송
        try {
            Account account = accountRepository.findByUser_UserCreatedId(userCreatedId)
                    .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));

            // 휴면 상태 검증
            validateDormantAccount(account);

            String email = account.getUser().getEmail();

            try {
                String code = mailService.sendCode(email);

                // redis 저장: (key: DORMANT_RELEASE_CODE:userCreatedId, value: 123456, TTL: 5분)
                redisTemplate.opsForValue().set(PREFIX + userCreatedId, code, LIMIT_TIME, TimeUnit.SECONDS);

            } catch (Exception e) {
                log.error("[인증번호] 메일 발송 실패: ", e);
                throw new MailSendException("인증코드 발송에 실패했습니다: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("[인증번호] 메일 발송 실패: 예상치 못한 에러");
            throw new MailSendException("인증코드 발송에 실패했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyCode(Long userCreatedId, String code) {   // 인증 번호 검증
        String savedCode = redisTemplate.opsForValue().get(PREFIX + userCreatedId);

        if (savedCode == null || !savedCode.equals(code)) {
            log.warn("[인증번호] 인증번호 검증 실패: 잘못된 인증번호 입력값");
            throw new InvalidCodeException("올바르지 않은 코드입니다.");
        }

        // 인증 성공 시 redis에서 삭제 (재사용 방지)
        redisTemplate.delete(PREFIX + userCreatedId);
    }

    public void validateDormantAccount(Account account) { // 계정의 상태 검증
        AccountStatusHistory latestHistory =
                accountStatusHistoryRepository.findFirstByAccountOrderByChangedAtDesc(account)
                        .orElseThrow(() -> {
                            log.error("[인증번호] 계정 상태 검증 실패: 계정 상태 누락");
                            return new StateNotFoundException("상태 정보가 없습니다.");
                        });

        if (!"DORMANT".equals(latestHistory.getStatus().getStatusName())) {
            log.warn("[인증번호] 계정 상태 검증 실패: 휴면 상태가 아닌 계정");
            throw new NotDormantAccountException("휴면 상태의 계정이 아닙니다.");
        }
    }

}
