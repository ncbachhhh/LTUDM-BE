package com.ncbachhhh.LTUDM.mapper;

import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.request.UserUpdateRequest;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.entity.User.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "backgroundUrl", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "showBirthday", ignore = true)
    @Mapping(target = "onlineStatus", ignore = true)
    @Mapping(target = "showEmail", ignore = true)
    @Mapping(target = "mentionSuggestions", ignore = true)
    @Mapping(target = "readReceipts", ignore = true)
    @Mapping(target = "notificationEnabled", ignore = true)
    @Mapping(target = "soundEnabled", ignore = true)
    @Mapping(target = "notificationSound", ignore = true)
    @Mapping(target = "themeMode", ignore = true)
    @Mapping(target = "chatColor", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(UserRegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "backgroundUrl", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "dob", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "showBirthday", ignore = true)
    @Mapping(target = "onlineStatus", ignore = true)
    @Mapping(target = "showEmail", ignore = true)
    @Mapping(target = "mentionSuggestions", ignore = true)
    @Mapping(target = "readReceipts", ignore = true)
    @Mapping(target = "notificationEnabled", ignore = true)
    @Mapping(target = "soundEnabled", ignore = true)
    @Mapping(target = "notificationSound", ignore = true)
    @Mapping(target = "themeMode", ignore = true)
    @Mapping(target = "chatColor", ignore = true)
    void updateUser(UserUpdateRequest request, @MappingTarget User user);

    UserResponse toUserResponse(User user);
}
