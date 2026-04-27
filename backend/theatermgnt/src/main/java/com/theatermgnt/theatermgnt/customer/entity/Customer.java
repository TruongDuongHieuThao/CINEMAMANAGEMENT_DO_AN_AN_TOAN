package com.theatermgnt.theatermgnt.customer.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import com.theatermgnt.theatermgnt.common.enums.Gender;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "customers")
@SQLDelete(sql = "UPDATE customers SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Customer extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    Account account;

    String firstName;
    String lastName;
    String address;
    String avatarUrl;
    String phoneNumber;

    @Builder.Default
    @Column(name = "loyalty_points", nullable = false)
    Integer loyaltyPoints = 0;

    @Enumerated(EnumType.STRING)
    Gender gender;

    LocalDate dob;
}
