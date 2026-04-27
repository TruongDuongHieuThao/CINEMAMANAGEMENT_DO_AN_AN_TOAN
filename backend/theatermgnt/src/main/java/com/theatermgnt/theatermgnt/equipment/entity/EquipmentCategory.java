package com.theatermgnt.theatermgnt.equipment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
@Table(name = "equipmentCategories")
@SQLDelete(sql = "UPDATE equipmentCategories SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class EquipmentCategory extends BaseEntity {
    @Column(nullable = false, unique = true)
    String name;

    String description;
}
