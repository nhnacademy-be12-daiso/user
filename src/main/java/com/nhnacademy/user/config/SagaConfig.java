package com.nhnacademy.user.config;

import com.nhnacademy.user.saga.SagaTopic;
import org.springframework.amqp.core.*;
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
