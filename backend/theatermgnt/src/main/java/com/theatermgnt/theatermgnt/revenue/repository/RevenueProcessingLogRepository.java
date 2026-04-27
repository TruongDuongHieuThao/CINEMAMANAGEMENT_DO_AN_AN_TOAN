package com.theatermgnt.theatermgnt.revenue.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.revenue.entity.RevenueProcessingLog;

public interface RevenueProcessingLogRepository extends JpaRepository<RevenueProcessingLog, String> {
    Optional<RevenueProcessingLog> findByPaymentId(String paymentId);

    boolean existsByPaymentId(String paymentId);
}
