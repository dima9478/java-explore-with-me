package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class EventMapper {
    public static Event toEvent(NewEventDto dto, User author, Category cat) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(cat)
                .author(author)
                .eventDate(dto.getEventDate())
                .paid(dto.getPaid())
                .createdOn(LocalDateTime.now())
                .location(dto.getLocation())
                .requestModeration(dto.getRequestModeration())
                .participantLimit(dto.getParticipantLimit())
                .publishedOn(null)
                .state(EventState.PENDING)
                .build();
    }

    public static List<EventFullDto> toEventFullDtoList(List<Event> events,
                                                        Map<Long, Long> confirmedRequests,
                                                        Map<Long, Long> eventViews) {
        return events.stream()
                .map(e -> toEventFullDto(
                        e,
                        confirmedRequests.getOrDefault(e.getId(), 0L),
                        eventViews.getOrDefault(e.getId(), 0L))
                )
                .collect(Collectors.toList());
    }

    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(CategoryMapper.toDto(event.getCategory()))
                .location(event.getLocation())
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .initiator(UserMapper.toShortDto(event.getAuthor()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.isRequestModeration())
                .views(views)
                .eventDate(event.getEventDate())
                .title(event.getTitle())
                .state(event.getState())
                .build();
    }

    public static List<EventShortDto> toShortDtoList(List<Event> events,
                                                     Map<Long, Long> confirmedRequests,
                                                     Map<Long, Long> eventViews) {
        return events.stream()
                .map(e -> toShortDto(
                        e,
                        confirmedRequests.getOrDefault(e.getId(), 0L),
                        eventViews.getOrDefault(e.getId(), 0L))
                )
                .collect(Collectors.toList());
    }

    private static EventShortDto toShortDto(Event event, Long confirmedRequests, Long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .paid(event.isPaid())
                .title(event.getTitle())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toShortDto(event.getAuthor()))
                .views(views)
                .build();
    }
}
