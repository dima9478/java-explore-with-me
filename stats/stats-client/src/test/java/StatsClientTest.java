import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(classes = {RestTemplateAutoConfiguration.class, StatsClient.class})
public class StatsClientTest {
    @Autowired
    private StatsClient client;

    @Test
    public void postHit_return201() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .ip("123.34.23.34")
                .timestamp("2020-10-23 12:34:56")
                .app("ewm-service")
                .uri("/events/1")
                .build();
        ResponseEntity<Void> resp = client.postHit(dto);

        Assertions.assertEquals(201, resp.getStatusCodeValue());
    }

    @Test
    public void getStats_return200() {
        ResponseEntity<List<ViewStats>> resp = client.getStats(LocalDateTime.now(),
                LocalDateTime.now().plusDays(20),
                List.of("/events/1", "/events"),
                true);

        Assertions.assertEquals(200, resp.getStatusCodeValue());
    }
}
