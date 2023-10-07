package ru.practicum.ewm.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.admin.dto.NewUserRequest;
import ru.practicum.ewm.admin.dto.UserDto;
import ru.practicum.ewm.admin.mapper.UserMapper;
import ru.practicum.ewm.common.error.ConflictException;
import ru.practicum.ewm.common.model.User;
import ru.practicum.ewm.common.param.PaginationRequest;
import ru.practicum.ewm.common.param.PaginationRequestConverter;
import ru.practicum.ewm.common.repository.UserRepository;
import ru.practicum.ewm.common.util.DbAvailabilityChecker;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@Service
@Validated
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DbAvailabilityChecker checker;

    @Override
    public UserDto addUser(@Valid NewUserRequest dto) {
        User user = UserMapper.newUserRequestToUser(dto);

        try {
            return UserMapper.toDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Integrity constraint has been violated", e.getMessage());
        }
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, @Valid PaginationRequest pagination) {
        List<User> users;
        if (ids != null) {
            users = userRepository.findAllByIdIn(ids);
        } else {
            users = userRepository.findAll(
                    PaginationRequestConverter.toPageable(pagination, Sort.by(Sort.Direction.ASC, "id"))
            ).toList();
        }

        return UserMapper.toDtoList(users);
    }

    @Override
    public void deleteUser(Long userId) {
        checker.checkUser(userId);

        userRepository.deleteById(userId);
    }
}
