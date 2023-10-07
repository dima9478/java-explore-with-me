package ru.practicum.ewm.admin.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.admin.dto.NewUserRequest;
import ru.practicum.ewm.admin.dto.UserDto;
import ru.practicum.ewm.common.model.User;
import ru.practicum.ewm.users.dto.UserShortDto;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class UserMapper {
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static UserShortDto toShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static List<UserDto> toDtoList(List<User> users) {
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public static User newUserRequestToUser(NewUserRequest dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }
}
