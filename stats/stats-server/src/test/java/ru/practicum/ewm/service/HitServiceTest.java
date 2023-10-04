package ru.practicum.ewm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.param.StatParams;
import ru.practicum.ewm.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HitServiceTest {
    @Mock
    private EndpointHitRepository repository;
    @InjectMocks
    private HitServiceImpl service;
    private StatParams params;

    @BeforeEach
    void setUp() {
        params = new StatParams(LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, false);
    }

    @Test
    void getUriHits_whenNoUrisAndNoUnique() {
        service.getUriHits(params);

        verify(repository, times(1)).getStats(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUriHits_whenUrisAndNoUnique() {
        params.setUris(List.of("/events/1"));

        service.getUriHits(params);

        verify(repository, times(1)).getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(List.class)
        );
    }

    @Test
    void getUriHits_whenNoUrisAndUnique() {
        params.setUnique(true);

        service.getUriHits(params);

        verify(repository, times(1)).getIpUniqueStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    void getUriHits_whenUrisAndUnique() {
        params.setUris(List.of("/events/1"));
        params.setUnique(true);

        service.getUriHits(params);

        verify(repository, times(1)).getIpUniqueStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(List.class)
        );
    }
}
