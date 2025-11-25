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

package com.nhnacademy.user.repository.point;

import com.nhnacademy.user.entity.point.PointHistory;
import com.nhnacademy.user.entity.user.User;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // DB가 유저의 포인트 잔액을 계산(+ EARN, - USE, + CANCEL)
    @Query("SELECT COALESCE(SUM(CASE WHEN ph.type = 'EARN' THEN ph.amount " +
            "WHEN ph.type = 'USE' THEN -ph.amount " +
            "WHEN ph.type = 'CANCEL' THEN ph.amount " + // 사용 취소면 다시 더해줘야 함
            "ELSE 0 END), 0) " +
            "FROM PointHistory ph WHERE ph.user = :user")
    BigDecimal getPointByUser(@Param("user") User user);

    // 특정 회원의 포인트 내역 페이징 조회
    Page<PointHistory> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

}
