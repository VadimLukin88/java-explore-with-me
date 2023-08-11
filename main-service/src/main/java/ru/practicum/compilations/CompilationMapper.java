package ru.practicum.compilations;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.models.Compilation;
import ru.practicum.events.models.Event;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static Compilation mapDtoToCompilation(NewCompilationDto newCompDto) {
        return Compilation.builder()
            .id(null)
            .title(newCompDto.getTitle())
            .pinned(newCompDto.getPinned())
            .eventList(null)
            .build();
    }

    public static Compilation mapDtoToCompilation(UpdateCompilationRequest updCompDto) {
        return Compilation.builder()
            .id(null)
            .title(updCompDto.getTitle())
            .pinned(updCompDto.getPinned())
            .eventList(null)
            .build();
    }

    public static CompilationDto mapCompilationToDto(Compilation compilation) {
        return CompilationDto.builder()
            .id(compilation.getId())
            .title(compilation.getTitle())
            .pinned(compilation.getPinned())
            .events(null)
            .build();
    }
}
