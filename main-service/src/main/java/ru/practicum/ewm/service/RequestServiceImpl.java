package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.util.DbAvailabilityChecker;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final DbAvailabilityChecker availabilityChecker;

    @Transactional
    @Override
    public ParticipationRequestDto addRequest(long userId, long eventId) {
        Request request;

        User user = availabilityChecker.checkUser(userId);
        Event event = availabilityChecker.checkEvent(eventId);
        validate(userId, event,
                requestRepository
                        .getEventRequestsCountByStatus(event.getId(), RequestStatus.CONFIRMED)
        );

        boolean makeConfirmed = !event.isRequestModeration() || event.getParticipantLimit() == 0;
        try {
            request = requestRepository.save(Request.builder()
                    .created(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                    .requestor(user)
                    .event(event)
                    .status(makeConfirmed ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Integrity constraint has been violated.", e.getMessage());
        }

        return RequestMapper.toDto(request);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        availabilityChecker.checkUser(userId);

        return RequestMapper.toDtoList(requestRepository.findByRequestorId(userId));
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        availabilityChecker.checkUser(userId);
        Request request = availabilityChecker.checkRequestByRequestor(userId, requestId);

        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toDto(request);
    }

    private void validate(long userId, Event event, Long requestsCount) {
        if (event.getAuthor().getId() == userId) {
            throw new ConflictException("Conditions were not met", "User can't post requests to his event");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Conditions were not met", "Event must be published");
        }
        if (event.getParticipantLimit() != 0 && requestsCount >= event.getParticipantLimit()) {
            throw new ConflictException("Cannot create request", "Participant limit was reached for the event");
        }
    }
}
