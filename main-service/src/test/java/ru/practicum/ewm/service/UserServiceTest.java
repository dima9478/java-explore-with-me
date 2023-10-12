package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository repository;
    @Mock
    private DbAvailabilityChecker availabilityChecker;
    @InjectMocks
    private UserServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("m@q.ru")
                .id(2L)
                .name("name2")
                .build();
    }

    @Test
    void addUser() {
        when(repository.save(any(User.class))).thenReturn(user);

        UserDto dto1 = service.addUser(NewUserRequest.builder()
                .email("m@q.ru")
                .name("name2").build());

        assertThat(dto1.getName(), equalTo(user.getName()));
        assertThat(dto1.getEmail(), equalTo(user.getEmail()));
        assertThat(dto1.getId(), notNullValue());
    }

    @Test
    void getUsers_whenIdsIsNull_thenMakePaginationReq() {
        List<Long> ids = null;
        when(repository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        List<UserDto> users = service.getUsers(ids, new PaginationRequest(10, 0));

        verify(repository, times(1)).findAll(any(Pageable.class));
        assertThat(users, empty());
    }

    @Test
    void getUsers_whenIds_thenReturnUsersWithIds() {
        List<Long> ids = List.of(2L);
        when(repository.findAllByIdIn(ids)).thenReturn(List.of(user));

        List<UserDto> users = service.getUsers(ids, new PaginationRequest(10, 0));

        verify(repository, times(1)).findAllByIdIn(ids);
        assertThat(users.size(), equalTo(1));
    }

    @Test
    void deleteUsers() {
        service.deleteUser(1L);

        verify(availabilityChecker, times(1)).checkUser(1L);
        verify(repository, times(1)).deleteById(1L);
    }
}
