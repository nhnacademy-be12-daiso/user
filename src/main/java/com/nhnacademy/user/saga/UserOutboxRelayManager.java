package com.nhnacademy.user.saga;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class UserOutboxRelayManager {

    private final UserOutboxRelayProcessor userOutboxRelayProcessor;

    // PaymentEventListener가 커밋된 이후 실행됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxCommitted(UserOutboxCommittedEvent event) {
        userOutboxRelayProcessor.processRelay(event.getOutboxId());
    }
}