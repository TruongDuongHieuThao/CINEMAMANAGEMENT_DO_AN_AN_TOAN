package com.theatermgnt.theatermgnt.review.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import com.theatermgnt.theatermgnt.customer.entity.Customer;
import com.theatermgnt.theatermgnt.movie.entity.Movie;
import com.theatermgnt.theatermgnt.screening.entity.Screening;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "movie_reviews",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"customer_id", "movie_id"})})
@SQLDelete(sql = "UPDATE movie_reviews SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class MovieReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id")
    Screening screening;

    @Column(nullable = false, precision = 3, scale = 1)
    BigDecimal rating;

    @Column(columnDefinition = "TEXT")
    String comment;

    @Column(name = "is_spoiler")
    Boolean isSpoiler = false;

    @Column(name = "helpful_count")
    Integer helpfulCount = 0;

    @Column(name = "unhelpful_count")
    Integer unhelpfulCount = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieReview)) return false;
        MovieReview review = (MovieReview) o;
        return id != null && id.equals(review.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
