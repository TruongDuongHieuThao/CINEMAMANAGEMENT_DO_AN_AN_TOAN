package com.theatermgnt.theatermgnt.seatType.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.theatermgnt.theatermgnt.common.entity.BaseEntity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "seatTypes")
@SQLDelete(sql = "UPDATE seatTypes SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class SeatType extends BaseEntity {

    String typeName;
    BigDecimal basePriceModifier;
}
