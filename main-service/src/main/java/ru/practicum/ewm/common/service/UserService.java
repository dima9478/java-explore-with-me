package ru.practicum.ewm.common.service;

import ru.practicum.ewm.admin.dto.NewUserRequest;
import ru.practicum.ewm.admin.dto.UserDto;
import ru.practicum.ewm.common.param.PaginationRequest;

import javax.validation.Valid;
import java.util.List;

public interface UserService {
    UserDto addUser(@Valid NewUserRequest dto);

    List<UserDto> getUsers(List<Long> ids, @Valid PaginationRequest pagination);

    void deleteUser(Long userId);
}
