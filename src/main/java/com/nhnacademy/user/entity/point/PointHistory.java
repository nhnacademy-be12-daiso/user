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

package com.nhnacademy.user.entity.point;

import com.nhnacademy.user.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "PointHistories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory { // 포인트 사용 내역

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private Long pointHistoryId;        // 포인트 사용 내역 고유 ID (PK, AI)

    @Column(name = "order_id")
    private Long orderId;               // 주문 ID (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_created_id", nullable = false)
    private User user;                  // Users 테이블 외래키 (FK)

    @Column(nullable = false, precision = 10, scale = 2)    // 99999999.99원까지
    private BigDecimal amount;          // 포인트 사용 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;                  // 포인트 타입

    @Column(length = 30)
    private String description;         // 포인트 사유

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 포인트 사용 날짜

    public PointHistory(User user, BigDecimal amount, Type type, String description) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

}
