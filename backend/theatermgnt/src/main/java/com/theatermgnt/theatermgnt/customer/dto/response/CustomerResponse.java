package com.theatermgnt.theatermgnt.customer.dto.response;

import com.theatermgnt.theatermgnt.common.dto.response.BaseUserResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class CustomerResponse extends BaseUserResponse {
    String customerId;
    Integer loyaltyPoints;
    Boolean noPassword;
}
