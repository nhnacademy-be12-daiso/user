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

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaConfig {

    // ----- saga를 위한 설정 ------

    private static final String BOOK_EXCHANGE = "team3.saga.book.exchange";
    @Value("${rabbitmq.queue.user}")
    private String USER_QUEUE;
    @Value("${rabbitmq.routing.book.deducted}")
    private String ROUTING_KEY_DEDUCTED;

    private static final String USER_EXCHANGE = "team3.saga.user.exchange";

    @Bean
    public DirectExchange bookExchange() {
        return new DirectExchange(BOOK_EXCHANGE);
    }

    @Bean
    public Queue userPointQueue() {
        return QueueBuilder.durable(USER_QUEUE)
                .withArgument("x-dead-letter-exchange", "team3.user.dlx") // 큐에서 문제가 생기면 해당 DLX로 보냄
                .withArgument("x-dead-letter-routing-key", "fail.user")
                .build();
    }

    @Bean
    public Binding bindingBookDeducted(Queue userPointQueue, DirectExchange bookExchange) {
        return BindingBuilder.bind(userPointQueue)
                .to(bookExchange)
                .with(ROUTING_KEY_DEDUCTED);
    }

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(USER_EXCHANGE);
    }

}
