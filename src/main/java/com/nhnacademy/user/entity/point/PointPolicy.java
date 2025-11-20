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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "PointPolicies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPolicy {  // 포인트 정책(관리자 전용)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_policy_id")
    private Long pointPolicyId;     // 포인트 적립 정책 고유 ID (PK, AI)

    @Length(max = 30)
    @Column(name = "policy_name", nullable = false)
    private String policyName;      // 포인트 정책 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Method method;          // 적립 방식

    // BigDecimal 사용법.. 익혀야 함
    // 참고: https://dev.gmarket.com/75
    @Column(name = "earn_point", nullable = false, precision = 10, scale = 2)   // 총 자릿수: 10, 소수점 오른쪽의 자릿수: 2
    private BigDecimal earnPoint;   // 적립액 or 적립률

    public void modifyPolicy(Method method, BigDecimal earnPoint) { // 값 변경을 위한 메소드 (관리자용)
        this.method = method;
        this.earnPoint = earnPoint;
    }

}
