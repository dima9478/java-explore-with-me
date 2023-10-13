package ru.practicum.ewm.error;

import lombok.Getter;

@Getter
public abstract class AbstractApiException extends RuntimeException {
    protected String reason;

    public AbstractApiException(String reason) {
        super();
        this.reason = reason;
    }

    public AbstractApiException(String reason, String message) {
        super(message);
        this.reason = reason;
    }
}
