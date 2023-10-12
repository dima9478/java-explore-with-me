package ru.practicum.ewm.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
public class RequestRepositoryIT {
    private RequestRepository requestRepository;
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

        Request request1 = Request.builder()
                .status(RequestStatus.CONFIRMED)
                .id(1L)
                .event(event1)
                .requestor(user2)
                .created(LocalDateTime.now())
                .build();
        Request request2 = Request.builder()
                .status(RequestStatus.REJECTED)
                .id(2L)
                .event(event2)
                .requestor(user1)
                .created(LocalDateTime.now())
                .build();

        requestRepository.save(request1);
        requestRepository.save(request2);
    }

    @Test
    void getEventsConfirmedRequestsCount() {
        List<EventRequestCount> counts =
                requestRepository.getEventsRequestsCountByStatus(List.of(1L), RequestStatus.CONFIRMED);

        assertThat(counts.size(), equalTo(1));
        assertThat(counts.get(0).getEventId(), equalTo(1L));
        assertThat(counts.get(0).getCount(), equalTo(1L));
    }

    @Test
    void getEventConfirmedRequestsCount() {
        long count = requestRepository.getEventRequestsCountByStatus(1L, RequestStatus.CONFIRMED);

        assertThat(count, equalTo(1L));
    }

    @Test
    void getAllByEventIdAndStatus() {
        List<Request> requests = requestRepository.getAllByEventIdAndStatus(1L, RequestStatus.CONFIRMED);

        assertThat(requests.size(), equalTo(1));
        assertThat(requests.get(0).getEvent().getId(), equalTo(1L));
        assertThat(requests.get(0).getStatus(), equalTo(RequestStatus.CONFIRMED));
    }

    @Test
    void getAllByEventId() {
        List<Request> requests = requestRepository.getAllByEventId(2L);

        assertThat(requests.size(), equalTo(1));
        assertThat(requests.get(0).getEvent().getId(), equalTo(2L));
    }

    @Test
    void findAllByIdIn() {
        List<Request> requests = requestRepository.findAllByIdIn(List.of(1L, 2L));

        assertThat(requests.size(), equalTo(2));
    }

    @Test
    void findByRequestorId() {
        List<Request> requests = requestRepository.findByRequestorId(2L);

        assertThat(requests.size(), equalTo(1));
        assertThat(requests.get(0).getRequestor().getId(), equalTo(2L));
    }

    @Test
    void getAllByIdAndRequestorId() {
        Request request = requestRepository.getAllByIdAndRequestorId(1L, 2L).get();

        assertThat(request.getId(), equalTo(1L));
        assertThat(request.getRequestor().getId(), equalTo(2L));
    }
}
