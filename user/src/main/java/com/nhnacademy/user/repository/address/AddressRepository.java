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

package com.nhnacademy.user.repository.address;

import com.nhnacademy.user.entity.address.Address;
import com.nhnacademy.user.entity.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // 주소가 여러 개인 회원이 새 기본 주소를 추가했을 때 일어나는 N+1 문제를 해결하기 위한 쿼리 메소드
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user")
    void clearAllDefaultsByUser(User user);

    // 특정 사용자의 주소 개수만 세는 메소드
    long countByUser(User user);

    // 특정 사용자의 모든 주소 목록 조회
    List<Address> findAllByUser(User user);

    // 주소 아이디와 사용자 정보로 주소 조회
    Optional<Address> findByAddressIdAndUser(Long addressId, User user);

}
