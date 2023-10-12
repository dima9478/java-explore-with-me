package ru.practicum.ewm;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Import;
import ru.practicum.ewm.configuration.JacksonConfig;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.ParticipationRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@Import(JacksonConfig.class)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class SerializationTest {
    private JacksonTester<ParticipationRequestDto> requestTester;
    private JacksonTester<EventFullDto> eventTester;

    @SneakyThrows
    @Test
    void testRequestDtoDate() {
        ParticipationRequestDto dto = ParticipationRequestDto.builder()
                .created(LocalDateTime.of(2014, 11, 23, 12, 34)).build();

        JsonContent<ParticipationRequestDto> content = requestTester.write(dto);

        assertThat(content).extractingJsonPathStringValue("$.created").isEqualTo("2014-11-23T12:34:00");
    }

    @SneakyThrows
    @Test
    void testEventDtoDate() {
        EventFullDto dto = EventFullDto.builder()
                .eventDate(LocalDateTime.of(2014, 11, 23, 12, 34))
                .createdOn(LocalDateTime.of(2013, 11, 23, 12, 34))
                .publishedOn(LocalDateTime.of(2014, 10, 23, 12, 34)).build();

        JsonContent<EventFullDto> content = eventTester.write(dto);

        assertThat(content).extractingJsonPathStringValue("$.eventDate").isEqualTo("2014-11-23 12:34:00");
        assertThat(content).extractingJsonPathStringValue("$.createdOn").isEqualTo("2013-11-23 12:34:00");
        assertThat(content).extractingJsonPathStringValue("$.publishedOn").isEqualTo("2014-10-23 12:34:00");
    }
}
