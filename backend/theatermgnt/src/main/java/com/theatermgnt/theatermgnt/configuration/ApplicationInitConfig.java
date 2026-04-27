package com.theatermgnt.theatermgnt.configuration;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.account.service.RegistrationService;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffAccountCreationRequest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    private PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.datasource",
            name = "driver-class-name",
            havingValue = "org.postgresql.Driver")
    ApplicationRunner applicationRunner(
            AccountRepository accountRepository,
            RegistrationService registrationService) {
        return args -> {
            // Check existed username "admin"
            if (accountRepository.findByUsername("admin").isEmpty()) {
                //                var roles = new HashSet<String>();
                //                roles.add(Role.ADMIN.name());

                StaffAccountCreationRequest adminRequest = StaffAccountCreationRequest.builder()
                        .username("admin")
                        .password("admin")
                        .build();

                registrationService.createAdminAccount(adminRequest);
                log.warn("Admin account has been created with default password: admin, please change it");
            }
        };
    }
}
