package ru.practicum.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {

    private final HttpStatus httpStatus;
    public ValidationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
