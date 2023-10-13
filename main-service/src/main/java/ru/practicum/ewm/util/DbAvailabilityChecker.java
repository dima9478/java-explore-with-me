package ru.practicum.ewm.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.*;

@Component
@RequiredArgsConstructor
public class DbAvailabilityChecker {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final RequestRepository requestRepository;
    private final String reason = "The required object was not found";
    private final String messagePattern = "%s with id=%d was not found";

    public User checkUser(long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(
                        reason,
                        String.format(messagePattern, "User", userId))
        );
    }

    public Category checkCategory(long catId) {
        return categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException(
                        reason,
                        String.format(messagePattern, "Category", catId)
                )
        );
    }

    public Event checkEvent(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(
                        reason,
                        String.format(messagePattern, "Event", eventId)
                )
        );
    }

    public Event checkEventByState(long eventId, EventState state) {
        return eventRepository.findByIdAndState(eventId, state).orElseThrow(
                () -> new NotFoundException(
                        reason,
                        String.format(messagePattern, "Event", eventId)
                )
        );
    }

    public Event checkEventByAuthor(long eventId, long userId) {
        return eventRepository.findByIdAndAuthorId(eventId, userId).orElseThrow(
                () -> new NotFoundException(
                        reason,
                        String.format(messagePattern, "Event", eventId)
                )
        );
    }

    public Compilation checkCompilation(long compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException(
                        reason,
                        String.format(messagePattern, "Compilation", compId)
                )
        );
    }

    public Request checkRequestByRequestor(long userId, long requestId) {
        return requestRepository.getAllByIdAndRequestorId(requestId, userId).orElseThrow(
                () -> new NotFoundException(
                        reason,
                        String.format(messagePattern, "Request", requestId)
                )
        );
    }
}
