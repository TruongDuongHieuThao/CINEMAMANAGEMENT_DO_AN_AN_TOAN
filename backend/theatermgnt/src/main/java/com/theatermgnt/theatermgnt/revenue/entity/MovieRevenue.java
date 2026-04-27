package com.theatermgnt.theatermgnt.revenue.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "movie_revenue")
public class MovieRevenue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String movieId;

    @Column(nullable = false)
    String cinemaId;

    @Column(nullable = false)
    LocalDate reportDate;

    @Column(nullable = false)
    Integer totalTicketsSold;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal totalRevenue;
}
