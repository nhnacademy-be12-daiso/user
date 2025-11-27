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

import static org.assertj.core.api.Assertions.assertThat;

import com.nhnacademy.user.entity.user.Status;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class StatusRepositoryTest {

    @Autowired
    private StatusRepository statusRepository;

    @Test
    @DisplayName("상태 이름으로 조회 성공")
    void test1() {
        Status status = new Status("ACTIVE");
        statusRepository.save(status);

        Optional<Status> result = statusRepository.findByStatusName("ACTIVE");

        assertThat(result).isPresent();
        assertThat(result.get().getStatusName()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("존재하지 않는 상태 이름 조회 시 Empty 반환")
    void test2() {
        Optional<Status> result = statusRepository.findByStatusName("WEIRD_STATUS");

        assertThat(result).isEmpty();
    }

}
