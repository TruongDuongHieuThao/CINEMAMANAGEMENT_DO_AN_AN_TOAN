package com.theatermgnt.theatermgnt.customer.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerLoyaltyPointsResponse {
    Integer loyaltyPoints;
}
