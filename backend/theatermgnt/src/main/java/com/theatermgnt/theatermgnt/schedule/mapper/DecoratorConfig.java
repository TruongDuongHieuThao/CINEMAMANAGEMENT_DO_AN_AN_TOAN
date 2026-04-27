package com.theatermgnt.theatermgnt.schedule.mapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DecoratorConfig {

    @Bean
    @Primary
    public WorkScheduleMapper workScheduleMapper(WorkScheduleMapper delegate, WorkScheduleMapperDecorator decorator) {
        return decorator;
    }

    @Bean("delegate")
    public WorkScheduleMapper delegate() {
        return new WorkScheduleMapperImpl();
    }
}
