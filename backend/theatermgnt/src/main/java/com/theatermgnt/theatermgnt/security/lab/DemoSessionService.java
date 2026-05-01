package com.theatermgnt.theatermgnt.security.lab;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DemoSessionService {
    // In-memory store for demo sessions (sessionToken -> userId)
    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    public String createSession(String userId) {
        String sessionToken = java.util.UUID.randomUUID().toString();
        sessionStore.put(sessionToken, userId);
        log.info("Created demo session for user: {}", userId);
        return sessionToken;
    }

    public String getUserId(String sessionToken) {
        return sessionStore.get(sessionToken);
    }

    public boolean isValidSession(String sessionToken) {
        return sessionStore.containsKey(sessionToken);
    }

    public void invalidateSession(String sessionToken) {
        sessionStore.remove(sessionToken);
        log.info("Invalidated demo session: {}", sessionToken);
    }
}