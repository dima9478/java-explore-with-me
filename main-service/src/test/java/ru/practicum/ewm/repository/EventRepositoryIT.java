package ru.practicum.ewm.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class EventRepositoryIT {
    private EventRepository eventRepository;
    private UserRepository userRepository;
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
        Category category2 = new Category(2L, "cat2");

        category = categoryRepository.save(category);
        category2 = categoryRepository.save(category2);

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
                .category(category2)
                .createdOn(LocalDateTime.now())
                .description("desc2")
                .requestModeration(false)
                .participantLimit(0)
                .state(EventState.PUBLISHED)
                .title("title2")
                .build();
        eventRepository.save(event1);
        eventRepository.save(event2);
    }

    @Test
    void findAllByCategoryId() {
        List<Event> events = eventRepository.findAllByCategoryId(2L);

        assertThat(events.size(), equalTo(1));
        assertThat(events.get(0).getId(), equalTo(2L));
    }

    @Test
    void findAllByAuthorId() {
        List<Event> events = eventRepository.findAllByAuthorId(1L, Pageable.ofSize(10));

        assertThat(events.size(), equalTo(1));
        assertThat(events.get(0).getAuthor().getId(), equalTo(1L));
    }

    @Test
    void findByIdAndAuthorId() {
        Event event = eventRepository.findByIdAndAuthorId(1L, 1L).get();

        assertThat(event.getId(), equalTo(1L));
        assertThat(event.getAuthor().getId(), equalTo(1L));
    }

    @Test
    void findAllByIdIn() {
        List<Event> events = eventRepository.findAllByIdIn(List.of(1L, 2L));

        assertThat(events.size(), equalTo(2));
        assertThat(events.get(0).getId(), anyOf(is(1L), is(2L)));
        assertThat(events.get(1).getId(), anyOf(is(1L), is(2L)));
    }

    @Test
    void findByIdAndState() {
        Event event = eventRepository.findByIdAndState(1L, EventState.PUBLISHED).get();

        assertThat(event.getId(), equalTo(1L));
        assertThat(event.getState(), equalTo(EventState.PUBLISHED));
    }
}
