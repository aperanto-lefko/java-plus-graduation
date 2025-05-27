package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.StatsDto;
import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<Stats, Long> {
    @Query("""
                SELECT new ru.practicum.dto.StatsDto(s.app, s.uri, count(s.ip))
                FROM Stats AS s
                WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris
                GROUP BY s.app, s.uri
                ORDER BY count(s.ip) DESC
            """)
    List<StatsDto> getStatsWithUri(LocalDateTime start,
                                   LocalDateTime end,
                                   List<String> uris);

    @Query("""
                SELECT new ru.practicum.dto.StatsDto(s.app, s.uri, count(s.ip))
                FROM Stats AS s
                WHERE s.timestamp BETWEEN :start AND :end
                GROUP BY s.app, s.uri
                ORDER BY count(s.ip) DESC
            """)
    List<StatsDto> getStatsWithoutUri(LocalDateTime start,
                                      LocalDateTime end);

    @Query("""
                SELECT new ru.practicum.dto.StatsDto(s.app, s.uri, count(DISTINCT s.ip))
                FROM Stats AS s
                WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris
                GROUP BY s.app, s.uri
                ORDER BY count(DISTINCT s.ip) DESC
            """)
    List<StatsDto> getStatWithUriWithUniqueIp(LocalDateTime start,
                                              LocalDateTime end,
                                              List<String> uris);

    @Query("""
                SELECT new ru.practicum.dto.StatsDto(s.app, s.uri, count(DISTINCT s.ip))
                FROM Stats AS s
                WHERE s.timestamp BETWEEN :start AND :end
                GROUP BY s.app, s.uri
                ORDER BY count(DISTINCT s.ip) DESC
            """)
    List<StatsDto> getStatsWithoutUriWithUniqueIp(LocalDateTime start,
                                                  LocalDateTime end);
}