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

package com.nhnacademy.user.saga;

import com.nhnacademy.user.entity.saga.UserOutbox;
import com.nhnacademy.user.exception.saga.ExternalServiceException;
import com.nhnacademy.user.repository.saga.UserOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserOutboxRelayProcessor {

    private final UserEventPublisher userEventPublisher;
    private final UserOutboxRepository userOutboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRelay(Long outboxId) {

        UserOutbox outbox = userOutboxRepository.findById(outboxId).orElseThrow();
        // <<<<<< 예외처리

        try {
            userEventPublisher.publishUserOutboxMessage(
                    outbox.getTopic(),
                    outbox.getRoutingKey(),
                    outbox.getPayload()
            );
            log.info("[User API] Order ID : {}", outbox.getAggregateId());
            outbox.markAsPublished();
            userOutboxRepository.save(outbox);

        } catch (ExternalServiceException e) { // 실패시 재시도 및 롤백
            if (outbox.getRetryCount() < 3) {
                outbox.incrementRetryCount();
                userOutboxRepository.save(outbox); // DB에 업데이트
            } else {
                outbox.markAsFailed();
                userOutboxRepository.save(outbox); // DB에 업데이트
                log.error("[User API] Outbox 메세지 최종 발행 실패 OutboxID : {}", outboxId);
            }

            throw e; // 예외 던져서 롤백 유도
        }
    }

}
