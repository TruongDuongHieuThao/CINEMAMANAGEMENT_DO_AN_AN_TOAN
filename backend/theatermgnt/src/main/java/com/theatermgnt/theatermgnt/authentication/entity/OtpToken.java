package com.theatermgnt.theatermgnt.authentication.entity;

import java.time.Instant;

import jakarta.persistence.*;

import com.theatermgnt.theatermgnt.account.entity.Account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "otp_tokens")
public class OtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String code;

    @Column(nullable = false)
    Instant expiryTime;

    @OneToOne
    @JoinColumn(nullable = false, name = "account_id")
    Account account;
}
