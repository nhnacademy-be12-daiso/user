package com.nhnacademy.user.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaConfig {

    // ----- saga를 위한 설정 ------

    private static final String BOOK_EXCHANGE = "team3.saga.book.exchange";
    @Value("${rabbitmq.queue.user}")
    private String USER_QUEUE;
    private static final String ROUTING_KEY_DEDUCTED = "inventory.deducted";

    private static final String USER_EXCHANGE = "team3.saga.user2.exchange";

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
