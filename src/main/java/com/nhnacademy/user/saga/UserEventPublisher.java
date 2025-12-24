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

import com.nhnacademy.user.exception.saga.ExternalServiceException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserEventPublisher {

    @Qualifier("outboxRabbitTemplate")
    private final AmqpTemplate rabbitTemplate;

    private final String USER_EXCHANGE = "team3.saga.user.exchange";
    @Value("${rabbitmq.routing.point.deducted}")
    private String ROUTING_KEY_DEDUCTED;

//    // 로컬 트랜잭션이 커밋된 후에 실행됨
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void publishPointDeductedEvent(OrderConfirmedEvent event) {
//        try {
//            rabbitTemplate.convertAndSend(
//                    USER_EXCHANGE,
//                    ROUTING_KEY_DEDUCTED,
//                    event
//            );
//
//            log.info("[User API] 재고 차감 성공 이벤트 발행 완료 : {}", ROUTING_KEY_DEDUCTED);
//
//        } catch (Exception e) {
//            log.warn("[User API] RabbitMQ 발행 실패 : {}", e.getMessage());
//            // TODO : Outbox 패턴 또는 재시도 로직 구현해야함!!!
//        }
//    }

    public void publishUserOutboxMessage(String topic, String routingKey, String payload) {
        try {
            byte[] body = payload.getBytes(StandardCharsets.UTF_8);

            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON); // 👈 핵심 수정
            properties.setContentEncoding("UTF-8");
            Message message = new Message(body);

            rabbitTemplate.send(topic, routingKey, message); // 직렬화 해서 생으로 보냄

            log.info("[User API] 다음 이벤트 발행 완료 : User API -> Coupon API");

        } catch (Exception e) {
            log.warn("[User API] RabbitMQ 발행 실패 : {}", e.getMessage());
            throw new ExternalServiceException("rabbitMQ 메세지 발행 실패");
        }
    }

}
