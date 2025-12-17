package com.nhnacademy.user.repository.saga;

import com.nhnacademy.user.entity.saga.UserOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOutboxRepository extends JpaRepository<UserOutbox, Long> {
}
