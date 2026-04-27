package com.theatermgnt.theatermgnt.bookingCombo.dto.response;

import java.util.List;

import com.theatermgnt.theatermgnt.combo.dto.response.ComboItemResponse;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComboCheckInResponse {
    String bookingComboId;
    int quantity;
    int remain;
    ComboResponse combo;
    List<ComboItemResponse> comboItemResponseList;
}
