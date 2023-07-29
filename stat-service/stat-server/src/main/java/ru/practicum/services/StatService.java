package ru.practicum.services;

import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    List<StatResponseDto> getStatistics(LocalDateTime startTime, LocalDateTime endTime, String[] uris, boolean unique);

    void addStatistics(StatRequestDto requestDto);
}
