package com.nhnacademy.user.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserEventPublisher {

    private final AmqpTemplate rabbitTemplate;

    private final String USER_EXCHANGE = "team3.saga.user.exchange";
    private final String ROUTING_KEY_DEDUCTED = "point.deducted";

    // 로컬 트랜잭션이 커밋된 후에 실행됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPointDeductedEvent(OrderConfirmedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    USER_EXCHANGE,
                    ROUTING_KEY_DEDUCTED,
                    event
            );
            log.info("[User API] 재고 차감 성공 이벤트 발행 완료 : {}", ROUTING_KEY_DEDUCTED);
        } catch(Exception e) {
            log.warn("[User API] RabbitMQ 발행 실패 : {}", e.getMessage());
            // TODO : Outbox 패턴 또는 재시도 로직 구현해야함!!!
        }
    }


}
