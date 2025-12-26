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
    private final SagaReplyService replyService;

    @Transactional
    public void handleEvent(OrderConfirmedEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
//            testService.process(); // 일부러 포인트 부족 터트리기
            // TODO 01 포인트 차감 로직 (양진영 님)
            /**
             *  본인들 서비스 주입받아서 로직 구현하시면 됩니다.
             *  매개변수로 넘어온 event DTO를 까보시면 필요한 정보들이 담겨 있습니다.
             *  그거 토대로 각자 로직에 구현해주면 됨 (재고 차감, 포인트 차감, 쿠폰 사용 처리)
             *
             *  만약 포인트 차감 중 오류가 발생한다?
             *  그럼 하단에 PointNotEnoughException 던지면 됩니다!
             *
             *  더 좋은 로직 있다면 추천 가능
             */

            log.error("[User API] 포인트 차감 성공 - Order : {}", event.getOrderId());

        } catch(PointNotEnoughException e) { // 포인트 부족 비즈니스 예외
            log.error("[User API] 포인트 부족으로 인한 차감 실패 - Order : {}", event.getOrderId());
            isSuccess = false;
            reason = "INSUFFICIENT_POINTS";
            throw e;
        } catch(Exception e) {
            log.error("[User API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            // 이렇게 예외 범위를 넓게 해놔야 무슨 에러가 터져도 finally 문이 실행됨
            throw e;
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
            replyService.send(event, reply, SagaTopic.REPLY_RK);
        }
    }

    @Transactional
    public void handleRollbackEvent(OrderCompensateEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            // TODO 02 포인트 '보상' 로직 (양진영 님)
            /**
             * 동일하게 서비스 주입받아서 하시면 되는데,
             * 여기서는 '뭔가 잘못돼서 다시 원복시키는 롤백'의 과정입니다.
             * 그니까 아까 차감했던 포인트를 다시 원복시키는 로직을 구현하시면 됩니다.
             */

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
            replyService.send(event, reply, SagaTopic.REPLY_COMPENSATION_RK);
        }
    }
}

