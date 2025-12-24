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

package com.nhnacademy.user.config;

import com.nhnacademy.user.entity.user.User;
import com.nhnacademy.user.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("dev123")
@RequiredArgsConstructor
public class UserSeedConfig {

    private final UserRepository userRepository;
    private final EntityManager em;

    @Bean
    public CommandLineRunner seedBirthdayUsers() {
        return args -> seedDecemberUsers(100_000);
    }

    @Transactional
    public void seedDecemberUsers(int total) {
        int batchSize = 1000;
        LocalDate birth = LocalDate.of(1990, 12, 10); // 12월 생일 고정(원하면 날짜 바꿔)

        List<User> buffer = new ArrayList<>(batchSize);

        for (int i = 1; i <= total; i++) {
            String userName = "dec-user-" + i;

            // unique 보장
            String phone = "010-9" + String.format("%07d", i);        // 010-90000001 ~
            String email = "dec_user_" + i + "@test.local";           // 유니크

            buffer.add(new User(userName, phone, email, birth));

            if (buffer.size() == batchSize) {
                userRepository.saveAll(buffer);
                userRepository.flush();
                em.clear();      // 1차 캐시 비우기 (메모리 폭발 방지)
                buffer.clear();
            }
        }

        if (!buffer.isEmpty()) {
            userRepository.saveAll(buffer);
            userRepository.flush();
            em.clear();
        }
    }

}
