package ru.practicum.ewm.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.admin.dto.NewUserRequest;
import ru.practicum.ewm.admin.dto.UserDto;
import ru.practicum.ewm.common.param.PaginationRequest;
import ru.practicum.ewm.common.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto postUser(@RequestBody NewUserRequest dto) {
        return service.addUser(dto);
    }

    @GetMapping
    List<UserDto> getUsers(
            @RequestParam List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return service.getUsers(ids, new PaginationRequest(size, from));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void deleteUser(@PathVariable long userId) {
        service.deleteUser(userId);
    }

}
