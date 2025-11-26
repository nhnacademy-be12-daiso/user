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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class DoorayMessageSender {

    private final DoorayClient doorayClient;

    public void send(String loginId, String message) {  // 사용자에게 훅 메시지를 보내는 메소드 (임시)
        // 실제로는 loginId로 유저 정보를 조회해서 그 사람의 훅 URL을 찾거나
        // 공용 봇으로 "유저 [loginId]님의 인증번호: ..."라고 보낼 수도 있음
        DoorayPayload payload = new DoorayPayload("휴면 계정 활성화 봇", message);

        log.debug("Dooray 메시지 발송 시도 - target: {}", loginId);

        try {
            doorayClient.send(payload);

        } catch (Exception e) {
            log.error("Dooray 메시지 발송 실패 - target: {}, error: {}", loginId, e.getMessage());
        }
    }

}
