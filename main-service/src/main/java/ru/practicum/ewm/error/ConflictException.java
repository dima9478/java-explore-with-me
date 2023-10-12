package ru.practicum.ewm.error;

import lombok.Getter;

@Getter
public class ConflictException extends AbstractApiException {
    public ConflictException(String reason, String message) {
        super(reason, message);
    }
}
