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

package com.nhnacademy.user.batch;

import com.nhnacademy.user.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class AccountScheduler {    // 휴면 계정 자동 전환 스케줄러

    private final AccountService accountService;

    //  Cron 표현식 설명 (cron = "초 분 시 일 월 요일 년")
    // ───────────────────────────────────────────────────
    // * : 모든 값
    // ? : 특정 값 없음
    // - : 범위 (예: MON-WED)
    // , : 여러 값 지정 (예: MON,WED,FRI)
    // / : 주기 설정 (예: 0/5 → 0분부터 5분 간격)
    // L : 마지막 (예: 월의 마지막 날, 요일의 마지막 요일)
    // W : 가장 가까운 평일 (예: 15W → 15일 기준 가장 가까운 평일)
    // # : 몇 번째 주의 요일 (예: 3#2 → 둘째 주 수요일)
    // ───────────────────────────────────────────────────
    @Scheduled(cron = "0 0 4 * * *")    // 매일 새벽 4시에 휴면 계정 전환 배치 실행
    public void runDormantAccount() {
        log.info("===== 휴면 계정 전환 배치 작업 시작 =====");

        try {
            accountService.dormantAccounts();

            log.info("===== 휴면 계정 전환 배치 작업 성공 =====");

        } catch (Exception e) {
            log.error("휴면 계정 전환 배치 작업 중 오류 발생", e);
        }
    }

}
