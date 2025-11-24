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

package com.nhnacademy.user.repository.user;

import com.nhnacademy.user.entity.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    // 휴면 전환 대상자
    // 조건 1: 마지막 로그인 일시(last_login_at)가 기준 시간(cutoffDate) 이전일 것
    // 조건 2: 현재 상태가 ACTIVE일 것 (가장 최신 StatusHistory가 ACTIVE)
    @Query(value = "SELECT * FROM Users u " +
            "WHERE u.last_login_at < :cutoffDate " +
            "AND (" +
            "   SELECT s.status_name " +
            "   FROM UserStatusHistories ush " +
            "   JOIN Statuses s ON ush.status_id = s.status_id " +
            "   WHERE ush.user_created_id = u.user_created_id " +
            "   ORDER BY ush.changed_at DESC LIMIT 1" +
            ") = 'ACTIVE'", nativeQuery = true)
    List<User> findDormantUser(@Param("cutoffDate") LocalDateTime lastLoginAtBefore);

}
