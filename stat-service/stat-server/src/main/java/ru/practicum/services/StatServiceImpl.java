package ru.practicum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.models.StatEntry;
import ru.practicum.models.StatMapper;
import ru.practicum.repositories.StatRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;

    @Autowired
    public StatServiceImpl(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    @Override
    public List<StatResponseDto> getStatistics(LocalDateTime startTime,
                                               LocalDateTime endTime,
                                               String[] uris,
                                               boolean unique) {
        List<StatResponseDto> dtoList;

         if (unique) {
             if (uris != null && uris.length > 0) {
                dtoList = statRepository.getStatByUrisFromUniqueIp(startTime, endTime, uris);
            } else {
                dtoList = statRepository.getAllStatFromUniqueIp(startTime, endTime);
            }
        } else {
            if (uris != null && uris.length > 0) {
                dtoList = statRepository.getStatByUris(startTime, endTime, uris);
            } else {
                dtoList = statRepository.getAllStat(startTime, endTime);
            }
        }
         if (dtoList == null) {
             return new ArrayList<>();
         }
        return dtoList;
    }

    @Override
    public void addStatistics(StatRequestDto requestDto) {
        StatEntry statEntry = StatMapper.mapDtoToEntry(requestDto);

        statRepository.save(statEntry);
    }
}
