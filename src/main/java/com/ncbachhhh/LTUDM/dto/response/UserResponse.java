package com.ncbachhhh.LTUDM.dto.response;

import com.ncbachhhh.LTUDM.entity.User.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String email;
    String username;
    String display_name;
    String avatar_url;
    UserRole role;
}
