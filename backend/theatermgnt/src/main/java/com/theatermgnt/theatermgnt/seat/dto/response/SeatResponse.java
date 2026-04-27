package com.theatermgnt.theatermgnt.seat.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatResponse {
    String id;
    String seatName;
    String rowChair;
    Integer seatNumber;
    String seatTypeId;
}
