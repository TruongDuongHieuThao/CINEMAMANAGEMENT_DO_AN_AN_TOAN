package com.theatermgnt.theatermgnt.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.payment.entity.Payment;
import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByTransactionCode(String transactionCode);

    List<Payment> findByInvoiceId(String invoiceId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatusIn(List<PaymentStatus> statuses);
}
