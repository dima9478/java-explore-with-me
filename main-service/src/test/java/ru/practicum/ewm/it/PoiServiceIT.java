package ru.practicum.ewm.it;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.PoiDto;
import ru.practicum.ewm.dto.PoiShortDto;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.Poi;
import ru.practicum.ewm.model.PoiState;
import ru.practicum.ewm.param.Bbox;
import ru.practicum.ewm.param.PaginationRequest;
import ru.practicum.ewm.repository.PoiRepository;
import ru.practicum.ewm.service.PoiServiceImpl;

import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AllArgsConstructor(onConstructor_ = @Autowired)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PoiServiceIT {
    private PoiServiceImpl poiService;
    private PoiRepository poiRepository;

    @BeforeEach
    void setUp() {
        Poi poi1 = Poi.builder()
                .id(1L)
                .name("pname")
                .impactRadius(60)
                .location(new Location(-78, 70))
                .state(PoiState.REJECTED)
                .build();
        Poi poi2 = Poi.builder()
                .id(2L)
                .name("tname")
                .impactRadius(80)
                .location(new Location(70, -80))
                .state(PoiState.CONFIRMED)
                .build();
        poiRepository.save(poi1);
        poiRepository.save(poi2);
    }

    @Test
    void getPois() {
        List<PoiShortDto> pois = poiService.getPois(
                null,
                new Bbox(-180, -90, 180, 90),
                new PaginationRequest(10, 0));

        assertThat(pois.size(), equalTo(1));
        assertThat("Output must contain only confirmed pois", pois.get(0).getId(), equalTo(2L));


        pois = poiService.getPois(
                "pname",
                new Bbox(-180, -90, 180, 90),
                new PaginationRequest(10, 0));

        assertThat("There must be no confirmed pois with pname", pois.size(), equalTo(0));
    }

    @Test
    void getPoisByAdmin() {
        List<PoiDto> pois = poiService.getPoisByAdmin(
                new Bbox(-180, -90, 180, 90),
                EnumSet.of(PoiState.REJECTED),
                new PaginationRequest(10, 0)
        );

        assertThat(pois.size(), equalTo(1));
        assertThat("Result must contain only rejected pois due to the filter",
                pois.get(0).getState(), equalTo(PoiState.REJECTED));


        pois = poiService.getPoisByAdmin(
                new Bbox(-90, 60, -70, 80),
                null,
                new PaginationRequest(10, 0)
        );

        assertThat(pois.size(), equalTo(1));
        assertThat(pois.get(0).getLocation(), equalTo(new Location(70, -80)));
    }
}
