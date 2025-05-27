package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StatsDto {

    @NotBlank(message = "app не должен быть пустым")
    private String app;

    @NotBlank(message = "uri не должен быть пустым")
    private String uri;

    @PositiveOrZero(message = "hits не должен быть отрицательным")
    private Long hits;
}