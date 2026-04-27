package com.theatermgnt.theatermgnt.account.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.theatermgnt.theatermgnt.account.dto.request.BaseAccountCreationRequest;
import com.theatermgnt.theatermgnt.account.dto.request.IAccountUpdateRequest;
import com.theatermgnt.theatermgnt.account.dto.request.PasswordCreationRequest;
import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.mapper.AccountMapper;
import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountService {
    AccountRepository accountRepository;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;

    public Account createAccount(BaseAccountCreationRequest request) {
        if (accountRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (accountRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        Account account = accountMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        return accountRepository.save(account);
    }

    // Consider
    public void updateAccount(Account account, IAccountUpdateRequest request) {
        if (accountRepository.existsByUsernameAndDeletedFalse(request.getPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_NUMBER_EXISTED);
        }
        accountMapper.updateAccount(account, request);
        accountRepository.save(account);
    }

    public void deleteAccount(String accountId) {
        Account account =
                accountRepository.findById(accountId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String suffix = "_deleted_" + System.currentTimeMillis();
        account.setUsername(account.getUsername() + suffix);
        account.setEmail(account.getEmail() + suffix);

        account.setDeleted(true);
        account.setIsActive(false);
        accountRepository.save(account);
    }

    public void createPassword(PasswordCreationRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }
        var context = SecurityContextHolder.getContext();
        String accountId = context.getAuthentication().getName();

        Account account =
                accountRepository.findById(accountId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (StringUtils.hasText(account.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_EXISTED);
        }
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        accountRepository.save(account);
    }
}
