package com.theatermgnt.theatermgnt.screening.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.screening.entity.Screening;
import com.theatermgnt.theatermgnt.screening.enums.ScreeningStatus;
import com.theatermgnt.theatermgnt.screening.repository.ScreeningRepository;
import com.theatermgnt.theatermgnt.screeningSeat.repository.ScreeningSeatRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScreeningStatusSchedulerService {

    ScreeningRepository screeningRepository;
    ScreeningSeatRepository screeningSeatRepository;
    /**
     * Chạy mỗi phút để cập nhật status
     * CHỈ query SCHEDULED và ONGOING - bỏ qua COMPLETED và CANCELLED
     */
    @Scheduled(cron = "0 * * * * *") // Giây 0 của mỗi phút
    @Transactional
    public void updateScreeningStatus() {
        LocalDateTime now = LocalDateTime.now();

        List<Screening> toOngoing =
                screeningRepository.findByStatusAndStartTimeLessThanEqual(ScreeningStatus.SCHEDULED, now);

        if (!toOngoing.isEmpty()) {
            toOngoing.forEach(screening -> {
                if (screening.getEndTime().isAfter(now)) {
                    screening.setStatus(ScreeningStatus.ONGOING);
                    screeningSeatRepository.lockAvailableSeatsByScreening(screening.getId());
                    log.info("Screening {} changed: SCHEDULED -> ONGOING", screening.getId());
                }
            });
            screeningRepository.saveAll(toOngoing);
        }

        List<Screening> toCompleted =
                screeningRepository.findByStatusAndEndTimeLessThanEqual(ScreeningStatus.ONGOING, now);

        if (!toCompleted.isEmpty()) {
            toCompleted.forEach(screening -> {
                screening.setStatus(ScreeningStatus.COMPLETED);
                log.info("Screening {} changed: ONGOING -> COMPLETED", screening.getId());
            });
            screeningRepository.saveAll(toCompleted);
        }

        log.debug("Status update completed: {} to ONGOING, {} to COMPLETED", toOngoing.size(), toCompleted.size());
    }
}
