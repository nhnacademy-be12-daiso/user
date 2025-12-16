package com.nhnacademy.user.event.listener;

import com.nhnacademy.user.event.WelcomeCouponEvent;
import com.nhnacademy.user.producer.CouponMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeCouponEventListener {

    private final CouponMessageProducer couponMessageProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommitSendWelcomeCoupon(WelcomeCouponEvent event){
        couponMessageProducer.sendWelcomeCouponMessage(event.userCreatedId());
        log.info("커밋 후 웰컴쿠폰 메시지 발행 userCreatedId={}", event.userCreatedId());
    }
}
