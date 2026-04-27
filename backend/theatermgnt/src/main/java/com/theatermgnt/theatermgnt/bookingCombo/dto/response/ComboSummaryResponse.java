package com.theatermgnt.theatermgnt.bookingCombo.dto.response;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComboSummaryResponse {
    String comboId;
    String comboName;
    int quantity;
    BigDecimal unitPrice;
    BigDecimal subtotal;
}
