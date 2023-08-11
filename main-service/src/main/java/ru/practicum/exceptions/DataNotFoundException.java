package ru.practicum.exceptions;

import org.springframework.http.HttpStatus;

public class DataNotFoundException extends RuntimeException {

    private final HttpStatus httpStatus;

    public DataNotFoundException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}