package com.theatermgnt.theatermgnt.review.mapper;

import org.mapstruct.*;

import com.theatermgnt.theatermgnt.customer.entity.Customer;
import com.theatermgnt.theatermgnt.movie.entity.Movie;
import com.theatermgnt.theatermgnt.review.dto.request.CreateReviewRequest;
import com.theatermgnt.theatermgnt.review.dto.request.UpdateReviewRequest;
import com.theatermgnt.theatermgnt.review.dto.response.ReviewResponse;
import com.theatermgnt.theatermgnt.review.entity.MovieReview;
import com.theatermgnt.theatermgnt.screening.entity.Screening;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface MovieReviewMapper {

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "screening", ignore = true)
    MovieReview toMovieReview(CreateReviewRequest request);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "screening", ignore = true)
    void updateMovieReviewFromRequest(UpdateReviewRequest request, @MappingTarget MovieReview review);

    ReviewResponse toReviewResponse(MovieReview review);

    ReviewResponse.CustomerInfo toCustomerInfo(Customer customer);

    ReviewResponse.MovieInfo toMovieInfo(Movie movie);

    @Mapping(source = "startTime", target = "startTime")
    ReviewResponse.ScreeningInfo toScreeningInfo(Screening screening);
}
