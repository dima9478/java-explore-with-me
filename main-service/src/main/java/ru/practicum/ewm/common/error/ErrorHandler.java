package ru.practicum.ewm.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflicts(ConflictException e) {
        ApiError resp = makeErrorResponse(e, HttpStatus.CONFLICT.name());
        resp.setReason(e.getReason());

        return resp;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        ApiError resp = makeErrorResponse(e, HttpStatus.NOT_FOUND.name());
        resp.setReason(e.getReason());

        return resp;
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            NumberFormatException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleAnnotatedValidation(RuntimeException e) {
        ApiError resp = makeErrorResponse(e, HttpStatus.BAD_REQUEST.name());
        resp.setReason("Invalid request parameters or body");

        return resp;
    }

    ApiError handleOthers(RuntimeException e) {
        ApiError resp = makeErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR.name());
        resp.setReason("Unrecognized error occurred.");

        return resp;
    }

    private ApiError makeErrorResponse(RuntimeException e, String status) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.toList()))
                .message(e.getMessage())
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
