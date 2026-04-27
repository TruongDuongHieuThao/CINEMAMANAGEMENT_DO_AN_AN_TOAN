package com.theatermgnt.theatermgnt.schedule.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.theatermgnt.theatermgnt.schedule.dto.response.WorkScheduleResponse;
import com.theatermgnt.theatermgnt.schedule.entity.WorkSchedule;
import com.theatermgnt.theatermgnt.staff.repository.StaffRepository;

@Component
public class WorkScheduleMapperDecorator implements WorkScheduleMapper {

    private final WorkScheduleMapper delegate;
    private StaffRepository staffRepository;

    @Autowired
    public WorkScheduleMapperDecorator(
            @Qualifier("delegate") WorkScheduleMapper delegate, StaffRepository staffRepository) {
        this.delegate = delegate;
        this.staffRepository = staffRepository;
    }

    @Override
    public WorkScheduleResponse toResponse(WorkSchedule ws) {

        WorkScheduleResponse response = delegate.toResponse(ws);

        if (staffRepository != null) {
            staffRepository.findById(ws.getUserId()).ifPresent(user -> {
                String fullName = user.getLastName() + " " + user.getFirstName();
                response.setUserName(fullName);
            });
        }

        return response;
    }
}
