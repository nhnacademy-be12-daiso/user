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

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 분산된 로컬 트랜잭션을 수행하기 위해 필요한 '확정된 최종 데이터'
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {

    private Long orderId;
    private Long userId;
    private Long outboxId;

    // 여기 있는건 이미 다 검증이 됐음을 전제로 한다
    private Map<Long, Integer> bookList;
    private Long totalAmount;
    private Long usedPoint; // 사용 포인트
    private Long savedPoint; // 적립 포인트
    private List<Long> usedCouponIds;

}
