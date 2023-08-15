package ru.practicum.compilations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@Validated
public class PublicCompilationController {

    private final CompilationService compilationService;

    @Autowired
    public PublicCompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    // Получение подборок событий
    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @PositiveOrZero
                                                @RequestParam(defaultValue = "0") int from,
                                                @Positive
                                                @RequestParam(defaultValue = "10") int size) {
        log.info(">>>> HTTP_GET: Получен запрос на получение подборок событий. "
                 + "Параметры запроса: pinned = {}, from = {}, size = {}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    // Получение подборки событий по его id
    // В случае, если подборки с заданным id не найдено, возвращает статус код 404
    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info(">>>> HTTP_GET: Получен запрос на получение подборки по ID. Compilation ID = {}", compId);
        return compilationService.getCompilationById(compId);
    }
}
