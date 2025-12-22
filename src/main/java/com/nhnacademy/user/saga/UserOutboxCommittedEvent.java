package com.nhnacademy.user.saga;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserOutboxCommittedEvent extends ApplicationEvent {
    private final Long outboxId;

    public UserOutboxCommittedEvent(Object source, Long outboxId) {
        super(source);
        this.outboxId = outboxId;
    }
}
