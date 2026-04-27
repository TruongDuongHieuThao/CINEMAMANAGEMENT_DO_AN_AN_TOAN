package com.theatermgnt.theatermgnt.combo.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComboResponse {
    String id;
    String name;
    String description;
    BigDecimal price;
    String imageUrl;
}
