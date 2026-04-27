package com.theatermgnt.theatermgnt.cinema.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import com.theatermgnt.theatermgnt.staff.entity.Staff;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cinemas")
@SQLDelete(sql = "UPDATE cinemas SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Cinema extends BaseEntity {
    String name;
    String address;
    String city;
    String phoneNumber;
    Integer buffer;

    @OneToOne
    @JoinColumn(name = "managerId", referencedColumnName = "id")
    Staff manager;
}
