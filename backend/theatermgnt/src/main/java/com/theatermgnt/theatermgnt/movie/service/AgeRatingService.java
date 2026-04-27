package com.theatermgnt.theatermgnt.movie.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.movie.dto.request.CreateAgeRatingRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.AgeRatingResponse;
import com.theatermgnt.theatermgnt.movie.entity.AgeRating;
import com.theatermgnt.theatermgnt.movie.mapper.AgeRatingMapper;
import com.theatermgnt.theatermgnt.movie.repository.AgeRatingRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AgeRatingService {

    AgeRatingRepository ageRatingRepository;
    AgeRatingMapper ageRatingMapper;

    // CREATE
    public AgeRatingResponse createAgeRating(CreateAgeRatingRequest request) {
        // Kiểm tra ID đã tồn tại chưa
        if (ageRatingRepository.existsById(request.getId())) {
            throw new AppException(ErrorCode.AGERATING_EXISTED);
        }

        // Kiểm tra Code đã tồn tại chưa
        if (ageRatingRepository.findByCode(request.getCode()).isPresent()) {
            throw new AppException(ErrorCode.AGERATING_CODE_EXISTED);
        }

        AgeRating ageRating = ageRatingMapper.toAgeRating(request);
        AgeRating savedAgeRating = ageRatingRepository.save(ageRating);
        log.info("Created age rating with id: {}", savedAgeRating.getId());
        return ageRatingMapper.toAgeRatingResponse(savedAgeRating);
    }

    // READ
    public List<AgeRatingResponse> getAllAgeRatings() {
        List<AgeRating> ageRatings = ageRatingRepository.findAll();
        return ageRatingMapper.toAgeRatingResponseList(ageRatings);
    }

    public AgeRatingResponse getAgeRatingById(String id) {
        AgeRating ageRating =
                ageRatingRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.AGERATING_NOT_EXISTED));
        return ageRatingMapper.toAgeRatingResponse(ageRating);
    }

    public AgeRatingResponse getAgeRatingByCode(String code) {
        AgeRating ageRating = ageRatingRepository
                .findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.AGERATING_NOT_EXISTED));
        return ageRatingMapper.toAgeRatingResponse(ageRating);
    }
}
