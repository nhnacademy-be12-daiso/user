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

package com.nhnacademy.user.producer;

import com.nhnacademy.user.dto.message.CouponIssueMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CouponMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public CouponMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public void sendWelcomeCouponMessage(Long userCreatedId) {
        CouponIssueMessage message = new CouponIssueMessage(userCreatedId);

        // 메시지 발송
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
//        rabbitTemplate.convertAndSend(exchangeName, routingKey, new CouponIssueMessage(999L));

        log.info("[RabbitMq] 웰컴 쿠폰 발급 메시지 전송 완료: userCreatedId={}", userCreatedId);
    }

}
