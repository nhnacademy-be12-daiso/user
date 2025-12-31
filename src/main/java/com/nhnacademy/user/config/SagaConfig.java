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

import com.nhnacademy.user.saga.SagaTopic;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaConfig {


    // ------ orchestration ------


    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SagaTopic.ORDER_EXCHANGE);
    }


    @Bean
    public Queue userQueue() {
        return new Queue(SagaTopic.USER_QUEUE);
    }

    @Bean
    public Queue userRollbackQueue() {
        return new Queue(SagaTopic.USER_COMPENSATION_QUEUE);
    }

    @Bean
    public Binding userBinding(Queue userQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(userQueue)
                .to(sagaExchange)
                .with(SagaTopic.USER_RK);
    }

    @Bean
    public Binding userRollbackBinding(Queue userRollbackQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(userRollbackQueue)
                .to(sagaExchange)
                .with(SagaTopic.USER_COMPENSATION_RK);
    }


    // =============================

    // ----- saga를 위한 설정 ------

    private static final String BOOK_EXCHANGE = "team3.saga.book.exchange";
    @Value("${rabbitmq.queue.user}")
    private String userQueue;
    @Value("${rabbitmq.routing.book.deducted}")
    private String routingKeyDeducted;

    private static final String USER_EXCHANGE = "team3.saga.user.exchange";

    @Bean
    public DirectExchange bookExchange() {
        return new DirectExchange(BOOK_EXCHANGE);
    }

    @Bean
    public Queue userPointQueue() {
        return QueueBuilder.durable(userQueue)
                .withArgument("x-dead-letter-exchange", "team3.user.dlx") // 큐에서 문제가 생기면 해당 DLX로 보냄
                .withArgument("x-dead-letter-routing-key", "fail.user")
                .build();
    }

    @Bean
    public Binding bindingBookDeducted(Queue userPointQueue, DirectExchange bookExchange) {
        return BindingBuilder.bind(userPointQueue)
                .to(bookExchange)
                .with(routingKeyDeducted);
    }

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(USER_EXCHANGE);
    }

}
