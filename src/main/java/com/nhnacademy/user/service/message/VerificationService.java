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

import com.nhnacademy.user.exception.message.InvalidCodeException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class VerificationService {  // 휴면 > 활성 전환을 위한 인증 처리 서비스 (Dooray Message Sender 기반)

    private final StringRedisTemplate redisTemplate;

    private final DoorayMessageSender doorayMessageSender;

    private static final String PREFIX = "ACTIVE_CODE:";

    private static final long LIMIT_TIME = 5 * 60;  // 5분

    public void sendCode(String loginId) {  // 인증 번호 발송
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        // key: ACTIVE_CODE:loginId, value: 123456, TTL: 5분)
        redisTemplate.opsForValue().set(PREFIX + loginId, code, LIMIT_TIME, TimeUnit.SECONDS);

        doorayMessageSender.send(loginId, "휴면 해제 인증번호 [" + code + "]를 입력해주세요.");
    }

    public void verifyCode(String loginId, String code) {   // 인증 번호 검증
        String savedCode = redisTemplate.opsForValue().get(PREFIX + loginId);

        if (savedCode == null || !savedCode.equals(code)) {
            throw new InvalidCodeException("올바르지 않은 코드입니다.");
        }

        // 인증 성공 시 redis에서 삭제 (재사용 방지)
        redisTemplate.delete(PREFIX + loginId);
    }

}
