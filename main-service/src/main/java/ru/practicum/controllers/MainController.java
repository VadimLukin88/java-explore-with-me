package ru.practicum.controllers;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.StatClient;
import ru.practicum.dto.StatRequestDto;

import java.util.List;

@Slf4j
@RestController
public class MainController {

    // код в этом классе просто тестирует работу клиента статистики

    private final StatClient statClient;

    @Autowired
    public MainController(StatClient statClient) {
        this.statClient = statClient;
    }

    @GetMapping("/hitStat")
    public ResponseEntity<Object> hitStats() {
        log.info("Отправляем тестовый запрос на добавление статистики");
        StatRequestDto dto1 = new StatRequestDto("app", "uri", "1.1.1.1", "2023-07-27 11:00:00");
        StatRequestDto dto2 = new StatRequestDto("app", "uri1", "2.2.2.2", "2023-07-27 11:00:00");
        statClient.hitStats(dto1);
        statClient.hitStats(dto2);

        return null;
    }

    @GetMapping("/getStat")
    public ResponseEntity<Object> getStats() {
        log.info("Отправляем тестовый запрос на добавление статистики");
        List<String> uris = List.of("uri","uri1");
        return statClient.getStats("2020-07-01 11:00:00", "2028-07-01 11:00:00", uris.toArray(String[]::new), true);
    }
}
