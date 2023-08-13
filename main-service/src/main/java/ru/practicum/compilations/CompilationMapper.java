package ru.practicum.compilations;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.models.Compilation;
import ru.practicum.events.EventMapper;

import java.util.stream.Collectors;

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
            .events(compilation.getEventList().stream().map(EventMapper::mapEventToShortDto).collect(Collectors.toList()))
            .build();
    }
}
