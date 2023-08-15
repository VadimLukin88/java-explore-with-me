package ru.practicum.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.exceptions.StatValidationException;
import ru.practicum.services.StatService;

import javax.validation.Valid;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@RestController
@Validated
public class StatController {

    private final StatService statService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/stats")
    public List<StatResponseDto> getStatistics(@RequestParam(required = true) String start,
                                               @RequestParam(required = true) String end,
                                               @RequestParam (required = false) String[] uris,
                                               @RequestParam(defaultValue = "false") boolean unique) {
        log.info("/>>> HTTP_GET: Received a request to get statistics. Parameters: StartTime = {}, EndTime = {}, "
                 + "URIs = {}, Unique = {}", start, end, uris, unique);
        LocalDateTime startTime;

        LocalDateTime endTime;

        try {
            startTime = LocalDateTime.parse(start, formatter);
            endTime = LocalDateTime.parse(end, formatter);
        } catch (DateTimeParseException e) {
            throw new StatValidationException("Can not parse time in request parameter", HttpStatus.BAD_REQUEST);
        }
        if (endTime.isBefore(startTime)) {
            throw new StatValidationException("End time before start time", HttpStatus.BAD_REQUEST);
        }
        return statService.getStatistics(startTime, endTime, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addStatistics(@Valid @RequestBody(required = true) StatRequestDto requestDto) {
        log.info("/>>> HTTP_POST: Received a request to add statistics. Parameters: {}", requestDto);
        statService.addStatistics(requestDto);
    }
}
