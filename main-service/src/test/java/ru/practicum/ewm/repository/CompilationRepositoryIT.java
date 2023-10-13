package ru.practicum.ewm.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class CompilationRepositoryIT {
    private CompilationRepository compilationRepository;
    private UserRepository userRepository;
    private EventRepository eventRepository;
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
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
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        Category category = new Category(1L, "cat1");

        category = categoryRepository.save(category);

        Event event1 = Event.builder()
                .id(1L)
                .eventDate(LocalDateTime.now().plusDays(2))
                .annotation("ann1")
                .paid(true)
                .author(user1)
                .category(category)
                .createdOn(LocalDateTime.now())
                .description("desc1")
                .requestModeration(false)
                .participantLimit(0)
                .state(EventState.PUBLISHED)
                .title("title1")
                .build();
        Event event2 = Event.builder()
                .id(2L)
                .eventDate(LocalDateTime.now().plusDays(2))
                .annotation("ann2")
                .paid(true)
                .author(user2)
                .category(category)
                .createdOn(LocalDateTime.now())
                .description("desc2")
                .requestModeration(false)
                .participantLimit(0)
                .state(EventState.PUBLISHED)
                .title("title2")
                .build();
        event1 = eventRepository.save(event1);
        event2 = eventRepository.save(event2);

        Compilation compilation1 = Compilation.builder()
                .events(List.of(event1))
                .pinned(true)
                .title("comp1")
                .build();
        Compilation compilation2 = Compilation.builder()
                .events(List.of(event1, event2))
                .pinned(false)
                .title("comp2")
                .build();

        compilationRepository.save(compilation1);
        compilationRepository.save(compilation2);
    }

    @Test
    void findAllByPinned() {
        List<Compilation> compilations = compilationRepository.findAllByPinned(true, Pageable.ofSize(10));

        assertThat(compilations.size(), equalTo(1));
        assertThat(compilations.get(0).isPinned(), equalTo(true));
    }
}
