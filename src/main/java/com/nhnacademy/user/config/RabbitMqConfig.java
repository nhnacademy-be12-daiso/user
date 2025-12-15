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
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    /**
     * 1. Exchange 구성 (User 서버가 메시지를 던질 우체국)
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    /**
     * 2. 메시지 변환기 설정 (Java 객체 -> JSON)
     * 이 설정이 있어야 "SimpleMessageConverter" 에러가 사라집니다!
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 3. RabbitTemplate 설정
     * 위에서 만든 JSON 변환기를 템플릿에 끼워줍니다.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        return rabbitTemplate;
    }

    // ----- saga를 위한 설정 ------

    private static final String BOOK_EXCHANGE = "team3.book.exchange";
    @Value("${rabbitmq.queue.user}")
    private String USER_QUEUE;
    private static final String ROUTING_KEY_DEDUCTED = "inventory.deducted";

    private static final String USER_EXCHANGE = "team3.user.exchange";

    @Bean
    public TopicExchange bookExchange() {
        return new TopicExchange(BOOK_EXCHANGE);
    }

    @Bean
    public Queue userPointQueue() {
        return new Queue(USER_QUEUE, true);
    }

    @Bean
    public Binding bindingBookDeducted(Queue userPointQueue, TopicExchange bookExchange) {
        return BindingBuilder.bind(userPointQueue)
                .to(bookExchange)
                .with(ROUTING_KEY_DEDUCTED);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }
}
