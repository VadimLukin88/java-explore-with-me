package ru.practicum.exceptions;

import org.springframework.http.HttpStatus;

public class StatValidationException extends RuntimeException {

    private final HttpStatus httpStatus;

    public StatValidationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
