package com.theatermgnt.theatermgnt.ticket.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketCheckInRequest {
    @NotNull
    String ticketCode;

    List<ComboUse> comboUseList;
}
