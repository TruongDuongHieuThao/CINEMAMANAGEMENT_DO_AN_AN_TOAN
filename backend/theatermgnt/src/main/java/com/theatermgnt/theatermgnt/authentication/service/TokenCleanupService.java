package com.theatermgnt.theatermgnt.authentication.service;

import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.authentication.repository.InvalidatedTokenRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TokenCleanupService {
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(cron = "0 0 0 * * * ", zone = "Asia/Ho_Chi_Minh")
    public void cleanUpExpiredTokens() {
        invalidatedTokenRepository.deleteAllExpiredTokens(new Date());
    }
}
