package com.theatermgnt.theatermgnt.ticket.dto.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TicketEmailView {
    String seatCode;
    String seatType;
    String ticketCode;
    BigDecimal ticketPrice;
}
