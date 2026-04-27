package com.theatermgnt.theatermgnt.staff.dto.response;

import java.util.Set;

import com.theatermgnt.theatermgnt.authorization.dto.response.RoleResponse;
import com.theatermgnt.theatermgnt.common.dto.response.BaseUserResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class StaffResponse extends BaseUserResponse {
    String staffId;
    String cinemaId;
    String jobTitle;
    String avatarUrl;
    Set<RoleResponse> roles;
}
