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

package com.nhnacademy.user.entity.saga;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "user_outbox")
public class UserOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 멱등성 키

    private Long aggregateId;
    private String aggregateType;

    private String topic; // exchange 이름
    private String routingKey;

    // retry_count는 발행 시도 횟수를 기록합니다.
    private int retryCount = 0;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status = OutboxStatus.PENDING;

    @CreationTimestamp // 생성 시 자동 기록
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 업데이트 시 자동 기록
    private LocalDateTime updatedAt;

    // 1. JPA 필수: 기본 생성자 (protected 사용 권장)
    protected UserOutbox() {
    }

    // 2. 새로운 Outbox 메시지 생성을 위한 생성자
    public UserOutbox(Long aggregateId, String aggregateType, String topic, String routingKey, String payload) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.topic = topic;
        this.routingKey = routingKey;
        this.payload = payload;
        // status와 retryCount는 기본값(PENDING, 0)을 사용합니다.
    }

    // 3. Relay 프로세스를 위한 상태 변경 메서드
    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
    }

    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}