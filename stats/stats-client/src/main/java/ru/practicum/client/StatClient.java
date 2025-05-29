package ru.practicum.client;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class StatClient {
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final RestClient.Builder restClientBuilder;
    private final String statsServerName;

    @Autowired
    public StatClient(DiscoveryClient discoveryClient,
                      @Value("${stats-server.name}") String statsServerName) {
       this.discoveryClient = discoveryClient;
       this.retryTemplate = createRetryTemplate();
       this.restClientBuilder = RestClient.builder();
       this.statsServerName = statsServerName;
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(3000L); // 3 секунды между попытками
        retryTemplate.setBackOffPolicy(backOffPolicy);
        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }
    private ServiceInstance getInstance() { //получение экземпляра сервиса от службы обнаружения
        try {
            return discoveryClient.getInstances(statsServerName).stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Нет доступных экземпляров сервиса " + statsServerName));
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка при получении экземпляра сервиса " + statsServerName, e);
        }
    }
    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(context -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    public ResponseEntity<Void> hit(@Valid HitDto hitDto) { //Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос пользователем с ip
        try {
            URI uri = makeUri("/hit");
            ResponseEntity<Void> response = restClientBuilder.build()
                    .post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(hitDto)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Сохранение статистики для {}", hitDto);
            return response;
        } catch (RestClientException e) {
            log.error("Ошибка выполнения запроса post сервером статистики для запроса {} : {}, трассировка:", hitDto, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<List<StatsDto>> getStats(String start, String end, List<String> uris, boolean unique) { // Получение статистики по посещениям.
        try {
            String path = buildStatsUri(start, end, uris, unique);
            URI uri = makeUri(path);
            ResponseEntity<List<StatsDto>> response = restClientBuilder.build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });
            log.info("Выполнен запрос GET с параметрами start={}, end={}, uris={}, unique={}:", start, end, uris, unique);
            return response;
        } catch (RestClientException e) {
            log.error("Ошибка выполнения запроса GET на сервер статистики с параметрами start={}, " +
                            "end={}, uris={}, unique={}: {}, трассировка:", start, end, uris, unique,
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    private String buildStatsUri(String start, String end, List<String> uris, Boolean unique) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start.replace(" ", "T"))
                .queryParam("end", end.replace(" ", "T"))
                .queryParam("uris", uris)
                .queryParam("unique", unique);
        return uriBuilder.toUriString();
    }
}
