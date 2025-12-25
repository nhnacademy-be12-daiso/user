package com.nhnacademy.user.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.entity.saga.UserOutbox;
import com.nhnacademy.user.exception.saga.FailedSerializationException;
import com.nhnacademy.user.repository.saga.UserOutboxRepository;
import com.nhnacademy.user.saga.event.SagaEvent;
import com.nhnacademy.user.saga.event.SagaReply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaReplyService {

    private final ObjectMapper objectMapper;
    private final UserOutboxRepository outboxRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(SagaEvent event, SagaReply reply, String key) {
        try {
            // 1. Outbox 엔티티 생성 및 저장
            UserOutbox outbox = new UserOutbox(
                    event.getOrderId(),
                    "USER",
                    SagaTopic.ORDER_EXCHANGE,
                    key,
                    objectMapper.writeValueAsString(reply)
            );

            outboxRepository.save(outbox);

            log.info("[Saga Reply] 독립 트랜잭션에 Outbox 저장 완료 (OrderID: {})", event.getOrderId());

            publisher.publishEvent(new UserOutboxCommittedEvent(this, outbox.getId()));

        } catch (JsonProcessingException e) {
            log.error("응답 메시지 직렬화 실패", e);
            throw new FailedSerializationException("응답 메시지 직렬화 실패");
        }
    }
}