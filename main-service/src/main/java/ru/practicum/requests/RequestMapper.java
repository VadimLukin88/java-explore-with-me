package ru.practicum.requests;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.events.models.Event;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.models.ParticipationRequest;
import ru.practicum.users.models.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {

    public static ParticipationRequestDto mapReqToDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
            .id(participationRequest.getId())
            .created(participationRequest.getCreated())
            .event(participationRequest.getEvent().getId())
            .requester(participationRequest.getRequester().getId())
            .status(participationRequest.getStatus())
            .build();
    }

    public static ParticipationRequest mapDtoToReq(ParticipationRequestDto requestDto, Event event, User requestor) {
        return ParticipationRequest.builder()
            .id(null)
            .created(requestDto.getCreated())
            .event(event)
            .requester(requestor)
            .status(null)
            .build();
    }
}
