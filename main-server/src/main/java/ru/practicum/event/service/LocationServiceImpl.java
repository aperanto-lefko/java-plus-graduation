package ru.practicum.event.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.LocationRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper mapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Location getLocation(Location location) {
        log.info("Поиск и сохранение местоположения {} ", location);
        return locationRepository.findByLatAndLon(location.getLat(), location.getLon())
                .orElseGet(() -> {
                    log.info("Локация не найдена. Будет сохранена новая: {}", location);
                    return location;
                });
    }
}
