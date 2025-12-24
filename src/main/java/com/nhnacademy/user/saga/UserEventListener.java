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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.entity.saga.UserDeduplicationLog;
import com.nhnacademy.user.entity.saga.UserOutbox;
import com.nhnacademy.user.exception.saga.FailedSerializationException;
import com.nhnacademy.user.exception.saga.InsufficientPointException;
import com.nhnacademy.user.repository.saga.UserDeduplicationRepository;
import com.nhnacademy.user.repository.saga.UserOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserEventListener {

    private final ApplicationEventPublisher publisher;
    private final UserDeduplicationRepository userDeduplicationRepository;
    private final ObjectMapper objectMapper;
    private final UserOutboxRepository userOutboxRepository;
    @Value("${rabbitmq.routing.point.deducted}")
    private String routingKey;

    @RabbitListener(queues = "${rabbitmq.queue.user}")
    @Transactional
    public void handleBookDeductedEvent(OrderConfirmedEvent event) {
        log.info("[User API] ===== 주문 확정 이벤트 수신됨 =====");
        log.info("[User API] Order ID : {}", event.getOrderId());

        Long msgId = event.getOrderId();
        if (userDeduplicationRepository.existsById(msgId)) {
            log.warn("[User API] 중복 이벤트 수신 및 무시 : {}", msgId);
            return;
        }

        try {
            // TODO 포인트 차감 로직

            UserDeduplicationLog logEntry = new UserDeduplicationLog(msgId.toString());
            userDeduplicationRepository.save(logEntry);

            try {
                UserOutbox outbox = new UserOutbox(
                        event.getOrderId(),
                        "USER",
                        "team3.saga.user.exchange",
                        routingKey,
                        objectMapper.writeValueAsString(event)
                );

                userOutboxRepository.save(outbox);
                publisher.publishEvent(new UserOutboxCommittedEvent(this, outbox.getId()));
                // 커밋 이벤트 발행

            } catch (FailedSerializationException e) {
                log.warn("객체 직렬화 실패");
                throw new FailedSerializationException("Failed to serialize event payload");
            }

            log.info("[User API] 포인트 내역 업데이트 성공");

        } catch (InsufficientPointException e) { // 커스텀 예외 처리 꼭 하기
            log.error("[User API] ===== 포인트 내역 업데이트 실패로 인한 보상 트랜잭션 시작 =====");
            log.error("[User API] Order ID : {}", event.getOrderId());

            throw e; // 트랜잭션이 걸려있으므로 예외를 던지면 DB 트랜잭션 롤백

        } catch (Exception e) {
            log.error("[User API] 이벤트 처리 중 예상치 못한 오류 발생 : {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

}
