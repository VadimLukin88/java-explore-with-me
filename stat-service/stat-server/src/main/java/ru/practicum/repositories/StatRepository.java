package ru.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.models.StatEntry;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<StatEntry, Long> {

    // Запрос кол-ва уникальных (уникальный IP) переходов по ссылкам, для заданного массива ссылок
    @Query("select new ru.practicum.dto.StatResponseDto(se.app, se.uri, count(distinct se.ip) as hits)"
            + " from StatEntry as se"
            + " where (se.timestamp between :startTime and :endTime) and (se.uri in :uris)"
            + " group by se.app, se.uri"
            + " order by hits desc")
    List<StatResponseDto> getStatByUrisFromUniqueIp(LocalDateTime startTime, LocalDateTime endTime, String[] uris);

    // Запрос общего кол-ва переходов по ссылкам, для заданного массива ссылок
    @Query("select new ru.practicum.dto.StatResponseDto(se.app, se.uri, count(se.id) as hits)"
        + " from StatEntry as se"
        + " where (se.timestamp between :startTime and :endTime) and (se.uri in :uris)"
        + " group by se.app, se.uri"
        + " order by hits desc")
    List<StatResponseDto> getStatByUris(LocalDateTime startTime, LocalDateTime endTime, String[] uris);

    // Запрос уникальных (уникальный IP) переходов по ссылкам для всех ссылок в базе
    @Query("select new ru.practicum.dto.StatResponseDto(se.app, se.uri, count(distinct se.ip) as hits)"
        + " from StatEntry as se"
        + " where (se.timestamp between :startTime and :endTime)"
        + " group by se.app, se.uri"
        + " order by hits desc")
    List<StatResponseDto> getAllStatFromUniqueIp(LocalDateTime startTime, LocalDateTime endTime);

    // Запрос общего кол-ва переходов по ссылкам, для всех ссылок в базе
    @Query("select new ru.practicum.dto.StatResponseDto(se.app, se.uri, count(se.id) as hits)"
        + " from StatEntry as se"
        + " where (se.timestamp between :startTime and :endTime)"
        + " group by se.app, se.uri"
        + " order by hits desc")
    List<StatResponseDto> getAllStat(LocalDateTime startTime, LocalDateTime endTime);
}
