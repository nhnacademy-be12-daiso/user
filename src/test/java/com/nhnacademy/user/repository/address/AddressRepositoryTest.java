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

import static org.assertj.core.api.Assertions.assertThat;

import com.nhnacademy.user.config.QueryDslConfig;
import com.nhnacademy.user.entity.address.Address;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.user.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(QueryDslConfig.class)
public class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("주소 저장 성공")
    void test1() {
        User user = new User("테스트_이름", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7));
        userRepository.save(user);

        Address address = new Address(user,
                "조선대학교", "61452", "광주광역시 동구 조선대길 146", "1층", true);
        addressRepository.save(address);

        Address found = addressRepository.findById(address.getAddressId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getAddressName()).isEqualTo("조선대학교");
        assertThat(found.getAddressDetail()).contains("1층");
        assertThat(found.isDefault()).isTrue();
        assertThat(found.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("기본 배송지 초기화(Bulk Update) 확인")
    void test2() {
        User user = new User("테스트", "010-0000-0000", "t@t.com", LocalDate.now());
        userRepository.save(user);

        Address addr1 = new Address(user, "집", "12345", "주소1", "상세1", true); // 기본
        addressRepository.save(addr1);

        Address addr2 = new Address(user, "회사", "09876", "주소2", "상세2", true); // 기본 (원래 이러면 안되지만 테스트니까)
        addressRepository.save(addr2);

        addressRepository.clearAllDefaultsByUser(user);

        entityManager.flush();
        entityManager.clear();

        Address found1 = addressRepository.findById(addr1.getAddressId()).orElseThrow();
        Address found2 = addressRepository.findById(addr2.getAddressId()).orElseThrow();

        assertThat(found1.isDefault()).isFalse();
        assertThat(found2.isDefault()).isFalse();
    }

    @Test
    @DisplayName("유저별 주소 개수 카운트")
    void test3() {
        User user = new User("카운트", "010-9999-9999", "c@c.com", LocalDate.now());
        userRepository.save(user);

        addressRepository.save(new Address(user, "1", "12345", "1", "1", false));
        addressRepository.save(new Address(user, "2", "09876", "2", "2", false));

        long count = addressRepository.countByUser(user);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("기본 배송지 1개 조회 - 기본 배송지가 있을 때")
    void test4() {
        User user = new User("배송지테스트", "010-1111-1111", "addr@test.com", LocalDate.now());
        userRepository.save(user);

        Address addr1 = new Address(user, "일반", "12345", "주소1", "상세1", false);
        addressRepository.save(addr1);

        Address addr2 = new Address(user, "기본", "09876", "주소2", "상세2", true);
        addressRepository.save(addr2);

        Address result = addressRepository.findFirstByUserAndIsDefaultTrue(user).orElse(null);

        assertThat(result).isNotNull();
        assertThat(result.getAddressName()).isEqualTo("기본");
        assertThat(result.isDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 1개 조회 - 없을 때 Null 반환")
    void test5() {
        User user = new User("배송지없음", "010-2222-2222", "noaddr@test.com", LocalDate.now());
        userRepository.save(user);

        Address addr1 = new Address(user, "일반", "12345", "주소1", "상세1", false);
        addressRepository.save(addr1);

        Address result = addressRepository.findFirstByUserAndIsDefaultTrue(user).orElse(null);

        assertThat(result).isNull();
    }

}
