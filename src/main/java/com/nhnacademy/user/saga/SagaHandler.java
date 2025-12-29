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

package com.nhnacademy.user.saga;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.user.dto.request.PointRequest;
import com.nhnacademy.user.entity.point.Type;
import com.nhnacademy.user.exception.point.PointNotEnoughException;
import com.nhnacademy.user.repository.saga.UserOutboxRepository;
import com.nhnacademy.user.saga.event.OrderCompensateEvent;
import com.nhnacademy.user.saga.event.OrderConfirmedEvent;
import com.nhnacademy.user.saga.event.OrderRefundEvent;
import com.nhnacademy.user.saga.event.SagaEvent;
import com.nhnacademy.user.saga.event.SagaReply;
import com.nhnacademy.user.service.point.PointService;
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

    private final PointService pointService;

    @Transactional
    public void handleEvent(SagaEvent event) {

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
            // 포인트 차감
            if (event instanceof OrderConfirmedEvent confirmedEvent) {
                if (confirmedEvent.getUsedPoint() != null && event.getOrderId() > 0) {
                    pointService.processPoint(new PointRequest(
                            confirmedEvent.getUserId(),
                            confirmedEvent.getUsedPoint(),
                            Type.USE,
                            "주문/결제시 포인트 사용"));
                }

                // 포인트 적립
                if (confirmedEvent.getSavedPoint() != null && confirmedEvent.getSavedPoint() > 0) {
                    pointService.processPoint(new PointRequest(
                            confirmedEvent.getUserId(),
                            confirmedEvent.getSavedPoint(),
                            Type.EARN,
                            "주문/결제 후 포인트 적립"));
                }
                log.debug("[User API] 포인트 차감 및 적립 성공 - Order : {}", event.getOrderId());
            }


            if (event instanceof OrderRefundEvent refundEvent) {
                // TODO 반품 금액을 포인트로 적립
                // 요구사항 보면 결제 금액은 포인트로 적립된다네요
                if (refundEvent.getRefundAmount() != null && event.getOrderId() > 0) {
                    pointService.processPoint(new PointRequest(
                            refundEvent.getUserId(),
                            refundEvent.getRefundAmount(),
                            Type.EARN,
                            "반품 처리에 따른 결제 금액 포인트 적립"));
                }
                log.debug("[User API] 반품시 결제 금액 포인트 적립 성공 - Order : {}", event.getOrderId());
            }

        } catch (PointNotEnoughException e) { // 포인트 부족 비즈니스 예외
            log.error("[User API] 포인트 부족으로 인한 차감 실패 - Order : {}", event.getOrderId());
            isSuccess = false;
            reason = "INSUFFICIENT_POINTS";
            throw e;

        } catch (Exception e) {
            log.error("[User API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            // 이렇게 예외 범위를 넓게 해놔야 무슨 에러가 터져도 finally 문이 실행됨
            throw e;

        } finally {
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
            // 사용했던 포인트 복구
            if (event.getUsedPoint() > 0) {
                pointService.processPoint(new PointRequest(
                        event.getUserId(),
                        event.getUsedPoint(),
                        Type.EARN,
                        "주문/결제 실패 포인트 복구"));
            }

            // 적립됐던 포인트 회수
            if (event.getSavedPoint() > 0) {
                pointService.processPoint(new PointRequest(
                        event.getUserId(),
                        event.getSavedPoint(),
                        Type.USE,
                        "주문/결제 실패 포인트 회수"
                ));
            }

            log.debug("[User API] 포인트 보상 성공 - Order : {}", event.getOrderId());

        } catch (Exception e) {
            log.error("[User API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
        } finally {
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

