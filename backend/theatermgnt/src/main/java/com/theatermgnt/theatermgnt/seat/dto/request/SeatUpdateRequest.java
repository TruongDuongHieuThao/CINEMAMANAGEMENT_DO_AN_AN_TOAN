package com.theatermgnt.theatermgnt.seat.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatUpdateRequest {
    String id;
    String rowChair;
    Integer seatNumber;
    String seatTypeId;
}
