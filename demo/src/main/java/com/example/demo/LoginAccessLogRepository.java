package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAccessLogRepository extends JpaRepository<LoginAccessLog, Long> {
}
