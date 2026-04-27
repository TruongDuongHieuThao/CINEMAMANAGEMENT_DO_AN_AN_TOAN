package com.theatermgnt.theatermgnt.movie.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.theatermgnt.theatermgnt.movie.dto.request.CreateAgeRatingRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.AgeRatingResponse;
import com.theatermgnt.theatermgnt.movie.entity.AgeRating;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AgeRatingMapper {

    AgeRatingResponse toAgeRatingResponse(AgeRating ageRating);

    List<AgeRatingResponse> toAgeRatingResponseList(List<AgeRating> ageRatings);

    AgeRating toAgeRating(CreateAgeRatingRequest request);
}
