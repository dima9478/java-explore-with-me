package ru.practicum.ewm.common.error;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private String reason;

    public NotFoundException(String reason) {
        super();
        this.reason = reason;
    }

    public NotFoundException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
}
