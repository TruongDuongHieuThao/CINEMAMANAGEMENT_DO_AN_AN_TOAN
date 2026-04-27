package com.theatermgnt.theatermgnt.equipment.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EquipmentCreationRequest {
    @NotBlank(message = "Equipment name is required")
    String name;

    @NotBlank(message = "Category ID is required")
    String categoryId;

    @NotBlank(message = "Room ID is required")
    String roomId;

    @NotBlank(message = "Serial number is required")
    String serialNumber;

    @NotBlank(message = "Status is required")
    String status;

    @NotNull(message = "Purchase date is required")
    @PastOrPresent(message = "Purchase date cannot be in the future")
    LocalDate purchaseDate;
}
