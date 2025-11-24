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

    // (cron = "초 분 시 일 월 요일 년")
    // * : 모든 값을 뜻합니다.
    // ? : 특정한 값이 없음을 뜻합니다.
    // - : 범위를 뜻합니다. (예) 월요일에서 수요일까지는 MON-WED로 표현
    // , : 특별한 값일 때만 동작 (예) 월,수,금 MON,WED,FRI
    // / : 시작시간 / 단위 (예) 0분부터 매 5분 0/5
    // L : 일에서 사용하면 마지막 일, 요일에서는 마지막 요일(토요일)
    // W : 가장 가까운 평일 (예) 15W는 15일에서 가장 가까운 평일 (월 ~ 금)을 찾음
    // # : 몇째주의 무슨 요일을 표현 (예) 3#2 : 2번째주 수요일
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
