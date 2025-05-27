package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HitDto {

    @NotBlank(message = "app не должен быть пустым")
    private String app;

    @NotBlank(message = "uri не должен быть пустым")
    private String uri;

    @NotBlank(message = "ip не должен быть пустым")
    private String ip;

    @NotNull(message = "timestamp не должен быть null")
    @PastOrPresent(message = "timestamp не должен быть в будущем")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}