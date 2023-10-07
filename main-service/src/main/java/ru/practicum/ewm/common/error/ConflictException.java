package ru.practicum.ewm.common.error;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
    private String reason;

    public ConflictException(String reason) {
        super();
        this.reason = reason;
    }

    public ConflictException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
}
