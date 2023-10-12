package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.param.PaginationRequest;

import javax.validation.Valid;
import java.util.List;

public interface UserService {
    UserDto addUser(@Valid NewUserRequest dto);

    List<UserDto> getUsers(List<Long> ids, @Valid PaginationRequest pagination);

    void deleteUser(Long userId);
}
