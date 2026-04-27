package com.theatermgnt.theatermgnt.equipment.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EquipmentCategoryUpdateRequest {
    String name;
    String description;
}
