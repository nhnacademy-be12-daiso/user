package com.nhnacademy.user.repository.saga;

import com.nhnacademy.user.entity.saga.UserDeduplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeduplicationRepository extends JpaRepository<UserDeduplicationLog, Long> {
}
