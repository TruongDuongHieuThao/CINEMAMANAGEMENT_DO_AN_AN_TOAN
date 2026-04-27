package com.theatermgnt.theatermgnt.equipment.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EquipmentCategoryCreationRequest {
    @NotBlank(message = "Category name is required")
    String name;

    String description;
}
