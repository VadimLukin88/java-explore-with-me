package ru.practicum.compilations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@Validated
public class AdminCompilationController {

    private final CompilationService compilationService;

    @Autowired
    public AdminCompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    //Добавление новой подборки (подборка может не содержать событий)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Valid @RequestBody NewCompilationDto newCompDto) {
        log.info(">>>> HTTP_POST: Получен запрос на добавление новой подборки. New CompilationDTO = {}", newCompDto);
        return compilationService.addCompilation(newCompDto);
    }

    //Удаление подборки
    @DeleteMapping("/{compId}")
    public void deleteCompilation(@RequestParam Long compId) {
        log.info(">>>> HTTP_DELETE: Получен запрос на удаление подборки. CompilationID = {}", compId);
        compilationService.deleteCompilation(compId);
    }

    // Обновить информацию о подборке
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody UpdateCompilationRequest updCompReq) {
        log.info(">>>> HTTP_PATCH: Получен запрос на изменение подборки. CompilationID = {}, CompilationDTO = {}", compId, updCompReq);
        return compilationService.updateCompilation(compId, updCompReq);
    }
}
