package com.theatermgnt.theatermgnt.screening.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.theatermgnt.theatermgnt.screening.entity.Screening;
import com.theatermgnt.theatermgnt.screening.enums.ScreeningStatus;

public interface ScreeningRepository extends JpaRepository<Screening, String> {
    List<Screening> findByMovieId(String movieId);

    List<Screening> findByRoomId(String roomId);

    boolean existsByMovieIdAndRoomIdAndStartTime(String movieId, String roomId, LocalDateTime startTime);

    boolean existsByMovieIdAndStartTimeBetween(String movieId, LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByMovieIdAndStatus(String movieId, ScreeningStatus status);

    List<Screening> findByStatusAndStartTimeLessThanEqual(ScreeningStatus status, LocalDateTime startTime);

    List<Screening> findByStatusAndEndTimeLessThanEqual(ScreeningStatus status, LocalDateTime endTime);

    List<Screening> findByStatusIn(List<ScreeningStatus> statuses);

    @Query(
            """
	SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END
	FROM Screening s
	WHERE s.room.id = :roomId
	AND (:excludeId IS NULL OR s.id <> :excludeId)
	AND s.startTime < :endTime
	AND s.endTime > :startTime
	""")
    boolean isTimeOverlap(String roomId, LocalDateTime startTime, LocalDateTime endTime, String excludeId);
}
