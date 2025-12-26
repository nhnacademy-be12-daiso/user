package com.nhnacademy.user.config;

import com.nhnacademy.user.entity.account.Account;
import com.nhnacademy.user.entity.account.Role;
import com.nhnacademy.user.entity.account.Status;
import com.nhnacademy.user.entity.user.Grade;
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
        return args -> seedDecemberUsers(50_000);
    }

    @Transactional
    public void seedDecemberUsers(int total) {
        int batchSize = 1000;
        LocalDate birth = LocalDate.of(1990, 12, 10);

        Grade defaultGrade = em.getReference(Grade.class, 1L);
        Status activeStatus = em.getReference(Status.class, 1L);

        List<User> buffer = new ArrayList<>(batchSize);

        for (int i = 1; i <= total; i++) {
            String userName = "dec-user-" + i;
            String phone = "010-9" + String.format("%07d", i);
            String email = "dec_user_" + i + "@test.local";

            User user = new User(userName, phone, email, birth, defaultGrade);

            String loginId = "dec_login_" + i;
            String password = "{noop}1234";
            Role role = Role.USER;

            // 연관관계 주인(Account)에서 user 지정하면 끝
            Account account = new Account(loginId, password, role, user, activeStatus);
            user.linkAccount(account);

            buffer.add(user);

            if (buffer.size() == batchSize) {
                userRepository.saveAll(buffer);
                userRepository.flush();
                em.clear();
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
