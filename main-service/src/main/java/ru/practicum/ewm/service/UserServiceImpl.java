package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.param.PaginationRequestConverter;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;

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
                    PaginationRequestConverter.toPageable(pagination)
            ).toList();
        }

        return UserMapper.toDtoList(users);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        checker.checkUser(userId);

        userRepository.deleteById(userId);
    }
}
