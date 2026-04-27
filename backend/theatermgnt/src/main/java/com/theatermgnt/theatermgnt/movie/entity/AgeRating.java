package com.theatermgnt.theatermgnt.movie.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.theatermgnt.theatermgnt.common.entity.BaseEntity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "age_ratings")
public class AgeRating extends BaseEntity {
    String code;
    String description;
}
