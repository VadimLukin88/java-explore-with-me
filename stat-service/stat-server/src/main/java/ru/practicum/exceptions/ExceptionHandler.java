package ru.practicum.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(ValidationException.class)
    @ResponseBody
    ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return new ResponseEntity<>(Map.of("Error message", e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
