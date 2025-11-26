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
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    // 휴면 전환 대상자
    // 1. 각 유저별로 가장 '최신' 상태 변경 이력의 ID를 찾습니다. (Group By)
    // 2. 위에서 찾은 ID로 실제 상태 정보를 조인합니다.
    // 3. 조건 필터링: 로그인 날짜 기준 + 현재 상태가 ACTIVE인 사람
    @Query(value = """
                SELECT u.*
                FROM Users u
                INNER JOIN (
                    SELECT user_created_id, MAX(user_status_history_id) as max_history_id
                    FROM UserStatusHistories
                    GROUP BY user_created_id
                ) latest_history ON u.user_created_id = latest_history.user_created_id
                INNER JOIN UserStatusHistories ush ON ush.user_status_history_id = latest_history.max_history_id
                INNER JOIN Statuses s ON ush.status_id = s.status_id
                WHERE u.last_login_at < :cutoffDate
                AND s.status_name = 'ACTIVE'
            """, nativeQuery = true)
    List<User> findDormantUser(@Param("cutoffDate") LocalDateTime lastLoginAtBefore);

    // 내 정보 조회 성능 최적화
    @Query("SELECT u FROM User u JOIN FETCH u.account WHERE u.userCreatedId = :userCreatedId")
    Optional<User> findByIdWithAccount(Long userCreatedId);

}
