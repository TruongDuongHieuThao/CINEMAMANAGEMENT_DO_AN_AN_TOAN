package com.theatermgnt.theatermgnt.account.mapper;

import org.mapstruct.*;

import com.theatermgnt.theatermgnt.account.dto.request.BaseAccountCreationRequest;
import com.theatermgnt.theatermgnt.account.dto.request.IAccountUpdateRequest;
import com.theatermgnt.theatermgnt.account.entity.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "password", ignore = true) // Password is handled separately
    @Mapping(target = "accountType", ignore = true)
    Account toAccount(BaseAccountCreationRequest request);

    @Mapping(target = "email", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true) // Password is handled separately
    @Mapping(target = "accountType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccount(@MappingTarget Account account, IAccountUpdateRequest request);
}
