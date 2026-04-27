package com.theatermgnt.theatermgnt.ticket.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketExpireScheduler {
    private final TicketService ticketService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void expireTicketsJob() {

        log.info("Running ticket expiration job");

        ticketService.expireTickets();

        log.info("Ticket expiration job finished");
    }
}
