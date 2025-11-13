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

import com.nhnacademy.user.entity.address.Address;
import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("주소 저장 성공")
    void test1() {
        User user = new User("테스트_이름", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7));
        userRepository.save(user);

        Address address = new Address(user, "조선대학교", "광주광역시 동구 조선대길 146", true);
        addressRepository.save(address);

        Address found = addressRepository.findById(address.getAddressId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getAddressName()).isEqualTo("조선대학교");
        assertThat(found.getAddressDetail()).contains("조선대");
        assertThat(found.isDefault()).isTrue();
        assertThat(found.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("User 삭제 시 Address도 함께 삭제(cascade)")
    void test2() {
        User user = new User("테스트_이름", "010-1234-5678",
                "test@test.com", LocalDate.of(2003, 11, 7));
        userRepository.save(user);

        Address address = new Address(user, "조선대학교", "광주광역시 동구 조선대길 146", true);
        addressRepository.save(address);

        // User 쪽 리스트에도 주소를 넣어줘서 양방향 관계를 맞춰줌
        user.addAddress(address);

        userRepository.delete(user);

        assertThat(addressRepository.existsById(address.getAddressId())).isFalse();
    }

}
