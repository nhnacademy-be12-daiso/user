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
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface UserRepository extends JpaRepository<User, Long>, UserQuerydslRepository {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    // 내 정보 조회 성능 최적화
    @Query("SELECT u FROM User u JOIN FETCH u.account WHERE u.userCreatedId = :userCreatedId")
    Optional<User> findByIdWithAccount(Long userCreatedId);

    // 포인트 수동 처리를 위한 조회 쿼리
    // 동시에 여러 요청이 들어오면 안 됨: 비관적 락 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")}) // 3초 대기 후 실패
    @Query("SELECT u FROM User u WHERE u.userCreatedId = :userCreatedId")
    Optional<User> findByIdForUpdate(Long userCreatedId);

}
