package ru.practicum.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.practicum.exceptions.dto.ApiError;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionsHandler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleMissingPathVariableException(MissingServletRequestParameterException e) {
        ApiError errorMessage = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .reason("Incorrectly made request.")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER))
            .build();
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleDataNotFoundException(DataNotFoundException e) {
        ApiError errorMessage = ApiError.builder()
            .status(e.getHttpStatus().getReasonPhrase())
            .reason(e.getMessage())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER))
            .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()))
            .build();
        return new ResponseEntity<>(errorMessage, e.getHttpStatus());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    ResponseEntity<ApiError> handleValidationException(ValidationException e) {
        ApiError errorMessage = ApiError.builder()
            .status(e.getHttpStatus().getReasonPhrase())
            .reason(e.getMessage())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER))
            .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()))
            .build();
        return new ResponseEntity<>(errorMessage, e.getHttpStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException e) {
        ApiError errorMessage = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .reason(e.getMessage())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER))
            .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()))
            .build();
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
}
