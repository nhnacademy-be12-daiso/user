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
    public void onMessage(SagaEvent event) {
        event.accept(this);
    }


    public void handleEvent(OrderConfirmedEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            // 포인트 차감
            if (event instanceof OrderConfirmedEvent confirmedEvent) {
                if (confirmedEvent.getUsedPoint() != null && confirmedEvent.getUsedPoint() > 0 &&
                        event.getOrderId() > 0) {
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
                    event.getEventId(),
                    event.getOrderId(),
                    "USER",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            replyService.send(event, reply, SagaTopic.REPLY_RK);
        }
    }

    // refund 전용
    public void handleEvent(OrderRefundEvent event) {
        // TODO 반품 금액을 포인트로 적립
        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유
        // 요구사항 보면 결제 금액은 포인트로 적립된다네요
        try {
            if (event.getRefundAmount() != null && event.getOrderId() > 0 &&
            event.getRefundAmount() > 0) {
                pointService.processPoint(new PointRequest(
                        event.getUserId(),
                        event.getRefundAmount(),
                        Type.EARN,
                        "반품 처리에 따른 결제 금액 포인트 적립"));
            }
            log.debug("[User API] 반품시 결제 금액 포인트 적립 성공 - Order : {}", event.getOrderId());
        } catch (PointNotEnoughException e) { // 포인트 부족 비즈니스 예외
            log.error("[User API] 비즈니스 오류로 인한 적립 실패 - Order : {}", event.getOrderId());
            isSuccess = false;
            reason = "POINT_ERROR";
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
                    event.getEventId(),
                    event.getOrderId(),
                    "USER",
                    isSuccess,
                    reason
            );
            // 응답 메시지 전송
            replyService.send(event, reply, SagaTopic.REPLY_RK);
        }
    }
    // 보상 전용
    public void handleEvent(OrderCompensateEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        SagaEvent originalEvent = event.getOriginalEvent();

        try {
            // 사용했던 포인트 복구
            if(originalEvent instanceof OrderConfirmedEvent confirmedEvent) {
                if (confirmedEvent.getUsedPoint() > 0 && confirmedEvent.getUsedPoint() != null) {
                    pointService.processPoint(new PointRequest(
                            confirmedEvent.getUserId(),
                            confirmedEvent.getUsedPoint(),
                            Type.EARN,
                            "주문/결제 실패 포인트 복구"));
                }

                // 적립됐던 포인트 회수
                if (confirmedEvent.getSavedPoint() > 0 && confirmedEvent.getSavedPoint() != null) {
                    pointService.processPoint(new PointRequest(
                            confirmedEvent.getUserId(),
                            confirmedEvent.getSavedPoint(),
                            Type.USE,
                            "주문/결제 실패 포인트 회수"
                    ));
                }

                log.debug("[User API] 포인트 보상 성공 - Order : {}", event.getOrderId());
            }
            if(originalEvent instanceof OrderConfirmedEvent confirmedEvent) {
                // TODO Refund의 보상 로직 작성
            }

        } catch (Exception e) {
            log.error("[User API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
        } finally {
            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getEventId(),
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

