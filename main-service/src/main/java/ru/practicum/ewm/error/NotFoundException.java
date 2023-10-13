package ru.practicum.ewm.error;

import lombok.Getter;

@Getter
public class NotFoundException extends AbstractApiException {
    public NotFoundException(String reason, String message) {
        super(reason, message);
    }
}
