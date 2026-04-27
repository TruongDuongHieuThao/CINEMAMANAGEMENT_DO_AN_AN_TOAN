package com.theatermgnt.theatermgnt.priceConfig.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.common.enums.DayType;
import com.theatermgnt.theatermgnt.common.enums.TimeSlot;
import com.theatermgnt.theatermgnt.priceConfig.entity.PriceConfig;

public interface PriceConfigRepository extends JpaRepository<PriceConfig, String> {
    List<PriceConfig> findBySeatTypeId(String seatTypeId);

    PriceConfig getPriceBySeatTypeIdAndDayTypeAndTimeSlot(String seatTypeId, DayType dayType, TimeSlot timeSlot);

    List<PriceConfig> findByDayTypeAndTimeSlot(DayType dayType, TimeSlot timeSlot);

    Optional<PriceConfig> findBySeatTypeIdAndDayTypeAndTimeSlot(String seatTypeId, DayType dayType, TimeSlot timeSlot);
}
