package com.theatermgnt.theatermgnt.security.lab;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CsrfTokenStore {
    // In-memory store for CSRF tokens (sessionToken -> csrfToken)
    private final Map<String, String> csrfStore = new ConcurrentHashMap<>();

    public String generateToken(String sessionToken) {
        String csrfToken = java.util.UUID.randomUUID().toString();
        csrfStore.put(sessionToken, csrfToken);
        log.info("Generated CSRF token for session: {}", sessionToken);
        return csrfToken;
    }

    public boolean isValid(String sessionToken, String csrfToken) {
        String storedToken = csrfStore.get(sessionToken);
        return storedToken != null && storedToken.equals(csrfToken);
    }

    public void invalidate(String sessionToken) {
        csrfStore.remove(sessionToken);
        log.info("Invalidated CSRF token for session: {}", sessionToken);
    }
}