package com.theatermgnt.theatermgnt.ticket.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class TicketCodeGenerator {
    public String generate() {
        return "TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
