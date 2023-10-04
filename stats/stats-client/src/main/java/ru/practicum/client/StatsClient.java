package ru.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatsClient {
    private final RestTemplate rest;
    private final DateTimeFormatter formatter;
    private final String serverUrl;

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.rest = builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Void> postHit(EndpointHitDto dto) {
        try {
            return rest.postForEntity("/hit", dto, Void.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    public ResponseEntity<List<ViewStats>> getStats(LocalDateTime start,
                                                    LocalDateTime end,
                                                    List<String> uris,
                                                    boolean unique) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("start", start.format(formatter));
        params.add("end", end.format(formatter));
        params.add("unique", unique ? "true" : "false");
        for (String uri: uris) {
            params.add("uris", uri);
        }
        URI uri = UriComponentsBuilder
                .fromHttpUrl(serverUrl)
                .path("/stats").queryParams(params).build().toUri();

        try {
            return rest.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<ViewStats>>() {});
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }
}
