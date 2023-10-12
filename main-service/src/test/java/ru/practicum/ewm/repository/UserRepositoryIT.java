package ru.practicum.ewm.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class UserRepositoryIT {
    private UserRepository repository;

    @Test
    void findAllByIdIn() {
        User user1 = User.builder()
                .name("name1")
                .id(1L)
                .email("email@r.com")
                .build();
        User user2 = User.builder()
                .name("name2")
                .id(2L)
                .email("email@e.com")
                .build();
        user1 = repository.save(user1);
        user2 = repository.save(user2);

        List<User> users = repository.findAllByIdIn(List.of(1L, 2L));

        assertThat(users.size(), equalTo(2));
    }
}
