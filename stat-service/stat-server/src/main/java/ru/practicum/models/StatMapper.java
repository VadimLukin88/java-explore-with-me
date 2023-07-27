package ru.practicum.models;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatMapper {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static StatEntry mapDtoToEntry(StatRequestDto requestDto) {
        LocalDateTime timestamp;

        try {
            timestamp = LocalDateTime.parse(requestDto.getTimestamp(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Can not parse time in request");
        }

        StatEntry statEntry = new StatEntry(null,
            requestDto.getApp(),
            requestDto.getUri(),
            requestDto.getIp(),
            timestamp);

        return statEntry;
    }

    public static StatResponseDto mapEntryToDto(StatEntry statEntry, long hits) {
        return new StatResponseDto(statEntry.getApp(), statEntry.getUri(), hits);
    }

}
