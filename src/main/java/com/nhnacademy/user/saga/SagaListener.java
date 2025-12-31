package com.nhnacademy.user.saga;

import com.nhnacademy.user.entity.saga.UserDeduplicationLog;
import com.nhnacademy.user.repository.saga.UserDeduplicationRepository;
import com.nhnacademy.user.saga.event.OrderCompensateEvent;
import com.nhnacademy.user.saga.event.OrderConfirmedEvent;
import com.nhnacademy.user.saga.event.SagaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Service
public class SagaListener {

    private final UserDeduplicationRepository deduplicationRepository;
    private final SagaHandler sagaHandler;

    @Transactional
    @RabbitListener(queues = SagaTopic.USER_QUEUE)
    public void onEvent(SagaEvent event) {

        // 중복 검사
        if(deduplicationRepository.existsByMessageId(String.valueOf(event.getEventId()))) {
            log.info("[Saga] 중복된 요청 무시 - Event ID : {} ", event.getEventId());
            return;
        }

        log.info("[Saga] 주문 이벤트 수신 - OrderID: {}", event.getOrderId());

        // 멱등성 보장
        deduplicationRepository.save(new UserDeduplicationLog(event.getEventId()));

        // 실제 작업은 핸들러가
        sagaHandler.onMessage(event);
    }

    // 보상 로직
    @Transactional
    @RabbitListener(queues = SagaTopic.USER_COMPENSATION_QUEUE)
    public void onCompensateEvent(SagaEvent event) {

        String dedupeKey = event.getEventId() + "_USER_COMP";

        // 중복 검사
        if(deduplicationRepository.existsByMessageId(dedupeKey)) {
            log.info("[Saga] 중복된 보상 요청 무시 - Event ID : {} ", event.getEventId());
            return;
        }

        log.info("[Saga] 보상 이벤트 수신 - OrderID: {}", event.getOrderId());

        // 멱등성 보장
        deduplicationRepository.save(new UserDeduplicationLog(dedupeKey));

        // 실제 작업은 핸들러가
        sagaHandler.onMessage(event);
    }
}
