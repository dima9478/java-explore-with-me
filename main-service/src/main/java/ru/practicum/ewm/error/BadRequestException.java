package ru.practicum.ewm.error;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    private String reason;

    public BadRequestException(String reason) {
        super();
        this.reason = reason;
    }

    public BadRequestException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
}

