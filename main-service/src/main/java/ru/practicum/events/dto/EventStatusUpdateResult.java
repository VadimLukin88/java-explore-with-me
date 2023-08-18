package ru.practicum.events.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// Dto для ответа на запрос массового изменения статуса событий (batch update)
@Data
public class EventStatusUpdateResult {
    private List<EventFullDto> publishedEvents = new ArrayList<>(); // список подтверждённых событий
    private List<EventFullDto> rejectedEvents = new ArrayList<>();  // список отклонённых событий
}
