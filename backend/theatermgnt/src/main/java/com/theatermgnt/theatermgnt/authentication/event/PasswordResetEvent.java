package com.theatermgnt.theatermgnt.authentication.event;

import com.theatermgnt.theatermgnt.account.entity.Account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Builder
public class PasswordResetEvent {
    Account account;
    String otpCode;
}
