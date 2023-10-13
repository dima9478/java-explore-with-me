package ru.practicum.ewm.error;

import lombok.Getter;

@Getter
public class BadRequestException extends AbstractApiException {
    public BadRequestException(String reason, String message) {
        super(reason, message);
    }
}

