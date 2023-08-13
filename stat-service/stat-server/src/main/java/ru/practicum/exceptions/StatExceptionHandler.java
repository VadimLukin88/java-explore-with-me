package ru.practicum.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.practicum.exceptions.dto.ApiError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
public class StatExceptionHandler {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @org.springframework.web.bind.annotation.ExceptionHandler(StatValidationException.class)
    @ResponseBody
    ResponseEntity<ApiError> handleValidationException(StatValidationException e) {
        ApiError errorMessage = ApiError.builder()
            .status(e.getHttpStatus().getReasonPhrase())
            .reason(e.getMessage())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER))
            .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList()))
            .build();
        return new ResponseEntity<>(errorMessage, e.getHttpStatus());
    }
}
