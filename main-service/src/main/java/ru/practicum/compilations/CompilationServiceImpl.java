package ru.practicum.compilations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.models.Compilation;
import ru.practicum.events.EventMapper;
import ru.practicum.events.EventRepository;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.models.Event;
import ru.practicum.exceptions.DataNotFoundException;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompilationServiceImpl implements CompilationService{

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CompilationServiceImpl(CompilationRepository compilationRepository, EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    // Получение подборки событий (для Public контроллера)
    // В случае, если по заданным фильтрам не найдено ни одной подборки, возвращает пустой список
    @Override
    @Transactional
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);

        return compilationRepository.findCompilationForUser(pinned, pageable).stream()
            .map(CompilationMapper::mapCompilationToDto)
            .collect(Collectors.toList());
    }

    // Получение события по Id (для Public контроллера)
    @Override
    @Transactional
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                                                       .orElseThrow(() -> new DataNotFoundException("Compilation not found", HttpStatus.NOT_FOUND));

        List<EventShortDto> eventList = compilation.getEventList().stream()
                                                                  .map(EventMapper::mapEventToShortDto)
                                                                  .collect(Collectors.toList());

        CompilationDto compDto = CompilationMapper.mapCompilationToDto(compilation);

        compDto.setEvents(eventList);
        return compDto;
    }

    // добавление подборки (для Admin контроллера)
    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompDto) {
        if(newCompDto.getPinned() == null) {
            newCompDto.setPinned(false);
        }
        if(newCompDto.getEvents() == null) {
            newCompDto.setEvents(new ArrayList<>());
        }
        Compilation compilation = CompilationMapper.mapDtoToCompilation(newCompDto);

        List<Event> eventList = new ArrayList<>();
        if (!newCompDto.getEvents().isEmpty()) {
            eventList = eventRepository.findAllById(newCompDto.getEvents());
        }
        compilation.setEventList(eventList);
        List<EventShortDto> eventDtoList = eventList.stream()
            .map(EventMapper::mapEventToShortDto)
            .collect(Collectors.toList());

        CompilationDto compDto = CompilationMapper.mapCompilationToDto(compilationRepository.save(compilation));

        compDto.setEvents(eventDtoList);
        return compDto;
    }

    // удаление подборки (для Admin контроллера)
    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                                                       .orElseThrow(() -> new DataNotFoundException("Compilation not found", HttpStatus.NOT_FOUND));

        compilationRepository.deleteById(compId);
    }

    // Обновить информацию о подборке (для Admin контроллера)
    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updCompReq) {
        Compilation compilation = compilationRepository.findById(compId)
                                                       .orElseThrow(() -> new DataNotFoundException("Compilation not found", HttpStatus.NOT_FOUND));

        if (updCompReq.getTitle() != null) {
            compilation.setTitle(updCompReq.getTitle());
        }
        if (updCompReq.getPinned() != null) {
            compilation.setPinned(updCompReq.getPinned());
        }
        List<Event> eventList = new ArrayList<>();
        if (updCompReq.getEvents() != null) {
            eventList = eventRepository.findAllById(updCompReq.getEvents());
            compilation.setEventList(eventList);
        }
        CompilationDto compDto = CompilationMapper.mapCompilationToDto(compilationRepository.save(compilation));

        List<EventShortDto> eventDtoList = eventList.stream()
            .map(EventMapper::mapEventToShortDto)
            .collect(Collectors.toList());

        compDto.setEvents(eventDtoList);
        return compDto;
    }
}
