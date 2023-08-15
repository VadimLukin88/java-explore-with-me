package ru.practicum.requests.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//Результат подтверждения/отклонения заявок на участие в событии
@Data
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();    // одобренные заявки
    private List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();     // отклонённые заявки
}
