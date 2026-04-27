package com.theatermgnt.theatermgnt.authentication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.authentication.entity.OtpToken;

public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {
    Optional<OtpToken> findByAccount(Account account);
}
