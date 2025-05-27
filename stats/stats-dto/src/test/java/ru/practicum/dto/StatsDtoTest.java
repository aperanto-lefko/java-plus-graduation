package ru.practicum.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatsDtoTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidStatsDto() {
        StatsDto stats = StatsDto.builder()
                .app("MyApp")
                .uri("/home")
                .hits(10L)
                .build();

        Set<ConstraintViolation<StatsDto>> violations = validator.validate(stats);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidStatsDtoWithBlankApp() {
        StatsDto stats = StatsDto.builder()
                .app("")
                .uri("/home")
                .hits(10L)
                .build();

        Set<ConstraintViolation<StatsDto>> violations = validator.validate(stats);
        assertEquals(1, violations.size());
        assertEquals("app не должен быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidStatsDtoWithBlankUri() {
        StatsDto stats = StatsDto.builder()
                .app("MyApp")
                .uri("")
                .hits(10L)
                .build();

        Set<ConstraintViolation<StatsDto>> violations = validator.validate(stats);
        assertEquals(1, violations.size());
        assertEquals("uri не должен быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidStatsDtoWithNegativeHits() {
        StatsDto stats = StatsDto.builder()
                .app("MyApp")
                .uri("/home")
                .hits(-1L)
                .build();

        Set<ConstraintViolation<StatsDto>> violations = validator.validate(stats);
        assertEquals(1, violations.size());
        assertEquals("hits не должен быть отрицательным", violations.iterator().next().getMessage());
    }
}