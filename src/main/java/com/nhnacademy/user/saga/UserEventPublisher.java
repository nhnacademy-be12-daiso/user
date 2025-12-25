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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserEventPublisher {

    @Qualifier("outboxRabbitTemplate")
    private final AmqpTemplate rabbitTemplate;

    public void publishUserOutboxMessage(String topic, String routingKey, String payload) {

        try {
            byte[] body = payload.getBytes(StandardCharsets.UTF_8);

            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON); // 👈 핵심 수정
            properties.setContentEncoding("UTF-8");
            Message message = new Message(body);

            rabbitTemplate.send(topic, routingKey, message); // 직렬화 해서 생으로 보냄

            log.info("[User API] ===== 메세지 발송됨 =====");
            log.info("[User API] Routing Key : {}", routingKey);
        } catch (Exception e) {
            log.warn("[User API] 메세지 발행 실패 : {}", e.getMessage());
            throw new ExternalServiceException("rabbitMQ 메세지 발행 실패");
        }
    }

}
