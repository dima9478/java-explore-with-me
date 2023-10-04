package ru.practicum.ewm.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ViewStats;
import ru.practicum.ewm.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest(properties = "db.name=ewm-test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EndpointHitRepositoryIT {
    @Autowired
    private EndpointHitRepository repository;

    @BeforeEach
    void setUp() {
        EndpointHit hit = EndpointHit.builder()
                .app("ewm-service")
                .uri("/events/1")
                .ip("192.126.34.22")
                .timestamp(LocalDateTime.now().minusDays(12))
                .build();
        EndpointHit hit2 = EndpointHit.builder()
                .app("ewm-service")
                .uri("/events/1")
                .ip("192.126.34.22")
                .timestamp(LocalDateTime.now().plusDays(12))
                .build();
        EndpointHit hit3 = EndpointHit.builder()
                .app("ewm-service")
                .uri("/events")
                .ip("192.126.34.22")
                .timestamp(LocalDateTime.now().plusDays(12))
                .build();

        repository.save(hit);
        repository.save(hit2);
        repository.save(hit3);
    }

    @Test
    void getStats() {
        List<ViewStats> stats = repository.getStats(LocalDateTime.now().minusDays(14),
                LocalDateTime.now().plusDays(14));

        Assertions.assertEquals(2, stats.size());
        Assertions.assertEquals(2L, stats.get(0).getHits());

        stats = repository.getStats(LocalDateTime.now().minusDays(14), LocalDateTime.now().plusDays(1));

        Assertions.assertEquals(1, stats.size());
        Assertions.assertEquals(1L, stats.get(0).getHits());
        Assertions.assertEquals("/events/1", stats.get(0).getUri());
    }

    @Test
    void getStatsCertainUri() {
        List<ViewStats> stats = repository.getStats(LocalDateTime.now().minusDays(14),
                LocalDateTime.now().plusDays(14), List.of("/events/1"));

        Assertions.assertEquals(1, stats.size());
        Assertions.assertEquals(2L, stats.get(0).getHits());
    }

    @Test
    void getIpUniqueStats() {
        List<ViewStats> stats = repository.getIpUniqueStats(LocalDateTime.now().minusDays(14),
                LocalDateTime.now().plusDays(14));

        Assertions.assertEquals(2, stats.size());
        Assertions.assertEquals(1L, stats.get(0).getHits());
    }

    @Test
    void getIpUniqueStatsCertainUri() {
        List<ViewStats> stats = repository.getIpUniqueStats(LocalDateTime.now().minusDays(14),
                LocalDateTime.now().plusDays(14), List.of("/events/1"));

        Assertions.assertEquals(1, stats.size());
        Assertions.assertEquals(1L, stats.get(0).getHits());
    }
 }
