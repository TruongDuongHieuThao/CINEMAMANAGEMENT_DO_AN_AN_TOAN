package com.theatermgnt.theatermgnt.ticket.dto.response;

import java.util.List;

import com.theatermgnt.theatermgnt.bookingCombo.dto.response.ComboCheckInResponse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketCheckInViewResponse {
    TicketResponse ticket;
    List<ComboCheckInResponse> comboCheckIn;
}
