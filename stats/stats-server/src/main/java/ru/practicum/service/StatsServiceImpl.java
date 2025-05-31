package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.exception.BadTimeException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Stats;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;
    private final StatRepository statRepository;

    @Override
    public StatsDto saveRequest(HitDto hitDto) {
        Stats stat = statsMapper.toEntity(hitDto);
        return statsMapper.toDto(statRepository.save(stat));
    }

    @Override
    public List<StatsDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (end == null || start == null) {
            throw new BadTimeException("The start and the end date cannot be null");
        }
        if (end.isBefore(start)) {
            throw new BadTimeException("The end date cannot be earlier than the start date");
        }

        if (uris.isEmpty()) {
            if (unique) {
                return statRepository.getStatsWithoutUriWithUniqueIp(start, end);
            } else {
                return statRepository.getStatsWithoutUri(start, end);
            }
        } else {
            if (unique) {
                return statRepository.getStatWithUriWithUniqueIp(start, end, uris);
            } else {
                return statRepository.getStatsWithUri(start, end, uris);
            }
        }
    }
}
