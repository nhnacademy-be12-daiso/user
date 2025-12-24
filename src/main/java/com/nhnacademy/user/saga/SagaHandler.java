package com.nhnacademy.user.saga;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.entity.saga.UserOutbox;
import com.nhnacademy.user.exception.point.PointNotEnoughException;
import com.nhnacademy.user.exception.saga.FailedSerializationException;
import com.nhnacademy.user.repository.saga.UserOutboxRepository;
import com.nhnacademy.user.saga.event.OrderCompensateEvent;
import com.nhnacademy.user.saga.event.OrderConfirmedEvent;
import com.nhnacademy.user.saga.event.SagaEvent;
import com.nhnacademy.user.saga.event.SagaReply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class SagaHandler {

    private final ObjectMapper objectMapper;
    private final UserOutboxRepository outboxRepository;
    private final ApplicationEventPublisher publisher;
    private final SagaTestService testService;

    @Transactional
    public void handleEvent(OrderConfirmedEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            // TODO 포인트 차감 로직
            // 서비스 주입받아서 하시면 됨

//            testService.process(); // 일부러 포인트 부족 터트리기


            // 로직 중에 포인트 부족하면 해당 커스텀 예외 던지시면 됩니다.
            // 더 좋은 방법 있으면 추천 좀

            log.error("[User API] 포인트 차감 성공 - Order : {}", event.getOrderId());

        } catch(PointNotEnoughException e) { // 포인트 부족 비즈니스 예외
            log.error("[User API] 포인트 부족으로 인한 차감 실패 - Order : {}", event.getOrderId());
            isSuccess = false;
            reason = "INSUFFICIENT_POINTS";
        } catch(Exception e) {
            log.error("[User API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            // 이렇게 예외 범위를 넓게 해놔야 무슨 에러가 터져도 finally 문이 실행됨
        }
        finally {
            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getOrderId(),
                    "USER",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            this.send(event, reply, SagaTopic.REPLY_RK);
        }
    }

    @Transactional
    public void handleRollbackEvent(OrderCompensateEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            // TODO 포인트 '보상' 로직
            // 서비스 주입받아서 하시면 됨

            log.error("[User API] 포인트 보상 성공 - Order : {}", event.getOrderId());

        } catch(Exception e) {
            log.error("[User API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
        }
        finally {
            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getOrderId(),
                    "USER",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            this.send(event, reply, SagaTopic.REPLY_COMPENSATION_RK);
        }
    }

    public void send(SagaEvent event, SagaReply reply, String key) {
        try {
            UserOutbox outbox = new UserOutbox(
                    event.getOrderId(),
                    "USER",
                    SagaTopic.ORDER_EXCHANGE,
                    key,
                    objectMapper.writeValueAsString(reply)
            );

            outboxRepository.save(outbox);

            log.info("[Saga Outbox] {} 토픽으로 메시지 저장 완료 (OrderID: {})", SagaTopic.REPLY_RK, event.getOrderId());
            publisher.publishEvent(new UserOutboxCommittedEvent(this, outbox.getId()));


        } catch (JsonProcessingException e) {
            log.warn("객체 직렬화 실패");
            throw new FailedSerializationException("객체 직렬화 실패");
        }
    }
}

