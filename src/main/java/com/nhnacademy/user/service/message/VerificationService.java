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
import com.nhnacademy.user.exception.account.NotDormantAccountException;
import com.nhnacademy.user.exception.message.InvalidCodeException;
import com.nhnacademy.user.exception.message.MailSendException;
import com.nhnacademy.user.exception.user.UserNotFoundException;
import com.nhnacademy.user.repository.account.AccountRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MailService 기반 휴면 계정 활성화를 위한 인증 처리 서비스
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class VerificationService {

    private final AccountRepository accountRepository;

    private final MailService mailService;

    private final StringRedisTemplate redisTemplate;

    private static final String DORMANT_STATUS = "DORMANT";

    private static final String PREFIX = "DORMANT_RELEASE_CODE:";

    private static final long LIMIT_TIME = (long) 5 * 60;  // 5분

    /**
     * 휴면 계정 활성화를 위한 인증번호를 발송하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     */
    @Transactional(readOnly = true)
    public void sendCode(Long userCreatedId) {
        try {
            Account account = accountRepository.findByUser_UserCreatedId(userCreatedId)
                    .orElseThrow(() -> {
                        log.warn("[VerificationService] 휴면 계정 활성화 인증번호 메일 전송 실패: 존재하지 않는 회원 ({})", userCreatedId);
                        return new UserNotFoundException("존재하지 않는 계정입니다.");
                    });

            // 휴면 상태 검증
            validateDormantAccount(account);

            String email = account.getUser().getEmail();

            String code = mailService.sendCode(email);

            // redis 저장: (key: DORMANT_RELEASE_CODE:userCreatedId, value: 123456, TTL: 5분)
            redisTemplate.opsForValue().set(PREFIX + userCreatedId, code, LIMIT_TIME, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("[VerificationService] 휴면 계정 활성화 인증번호 메일 전송 실패: {}", e.getMessage());
            throw new MailSendException("휴면 계정 활성화를 위한 인증번호 메일 전송 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    /**
     * 휴면 계정 활성화를 위한 인증번호를 검증하는 메소드
     *
     * @param userCreatedId Users 테이블 PK
     * @param code          휴면 계정 활성화를 위한 인증번호
     */
    @Transactional
    public void verifyCode(Long userCreatedId, String code) {
        String savedCode = redisTemplate.opsForValue().get(PREFIX + userCreatedId);

        if (savedCode == null || !savedCode.equals(code)) {
            log.warn("[VerificationService] 휴면 계정 활성화 인증번호 검증 실패: 잘못된 인증번호 입력값 ({})", code);
            throw new InvalidCodeException("올바르지 않은 코드입니다.");
        }

        // 인증 성공 시 redis에서 삭제 (재사용 방지)
        redisTemplate.delete(PREFIX + userCreatedId);
    }

    /**
     * 계정의 상태가 휴면(DORMANT) 상태인지 검증하는 메소드
     *
     * @param account 현재 상태를 검증할 계정
     */
    public void validateDormantAccount(Account account) {
        if (!DORMANT_STATUS.equals(account.getStatus().getStatusName())) {
            log.warn("[VerificationService] 계정 상태 검증 실패: 휴면 상태가 아닌 계정 ({})", account.getLoginId());
            throw new NotDormantAccountException("휴면 상태의 계정이 아닙니다.");
        }
    }

}
