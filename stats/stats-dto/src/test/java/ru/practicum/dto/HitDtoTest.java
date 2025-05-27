package ru.practicum.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HitDtoTest {

    private ObjectMapper objectMapper;

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testValidHitDto() {
        HitDto hit = HitDto.builder()
                .app("MyApp")
                .uri("/home")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<HitDto>> violations = validator.validate(hit);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidHitDtoWithBlankApp() {
        HitDto hit = HitDto.builder()
                .app("")
                .uri("/home")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<HitDto>> violations = validator.validate(hit);
        assertEquals(1, violations.size());
        assertEquals("app не должен быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidHitDtoWithBlankUri() {
        HitDto hit = HitDto.builder()
                .app("MyApp")
                .uri("")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<HitDto>> violations = validator.validate(hit);
        assertEquals(1, violations.size());
        assertEquals("uri не должен быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidHitDtoWithBlankIp() {
        HitDto hit = HitDto.builder()
                .app("MyApp")
                .uri("/home")
                .ip("")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<HitDto>> violations = validator.validate(hit);
        assertEquals(1, violations.size());
        assertEquals("ip не должен быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidHitDtoWithNullTimestamp() {
        HitDto hit = HitDto.builder()
                .app("MyApp")
                .uri("/home")
                .ip("127.0.0.1")
                .timestamp(null)
                .build();

        Set<ConstraintViolation<HitDto>> violations = validator.validate(hit);
        assertEquals(1, violations.size());
        assertEquals("timestamp не должен быть null", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidHitDtoWithFutureTimestamp() {
        HitDto hit = HitDto.builder()
                .app("MyApp")
                .uri("/home")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now().plusDays(1))
                .build();

        Set<ConstraintViolation<HitDto>> violations = validator.validate(hit);
        assertEquals(1, violations.size());
        assertEquals("timestamp не должен быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    public void testValidHitDtoWithJsonFormat() throws Exception {
        String json = "{\"app\":\"MyApp\",\"uri\":\"/home\",\"ip\":\"127.0.0.1\",\"timestamp\":\"2023-01-01 10:10:10\"}";

        HitDto hit = objectMapper.readValue(json, HitDto.class);

        assertEquals("MyApp", hit.getApp());
        assertEquals("/home", hit.getUri());
        assertEquals("127.0.0.1", hit.getIp());
        assertEquals(LocalDateTime.of(2023, 1, 1, 10, 10, 10), hit.getTimestamp());
    }

    @Test
    public void testJsonSerialization() throws Exception {
        HitDto hit = HitDto.builder()
                .app("MyApp")
                .uri("/home")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.of(2023, 1, 1, 10, 10, 10))
                .build();

        String json = objectMapper.writeValueAsString(hit);
        assertEquals("{\"app\":\"MyApp\",\"uri\":\"/home\",\"ip\":\"127.0.0.1\",\"timestamp\":\"2023-01-01 10:10:10\"}", json);
    }
}