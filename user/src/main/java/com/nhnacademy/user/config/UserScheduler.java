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

package com.nhnacademy.user.config;

import com.nhnacademy.user.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Component
public class UserScheduler {    // 휴면 계정 전환을 위한 스케줄러

    private final UserService userService;

    // (cron = "초 분 시 일 월 요일")
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void runDormantAccount() {   // 매일 새벽 4시에 휴면 계정 전환 배치 작업 실행
        log.info("===== 휴면 계정 전환 배치 작업 시작 =====");

        try {
            userService.dormantAccounts();

            log.info("===== 휴면 계정 전환 배치 작업 성공 =====");

        } catch (Exception e) {
            log.error("휴면 계정 전환 배치 작업 중 오류 발생", e);
        }
    }

}
