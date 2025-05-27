package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.validation.CreateValidationGroup;
import ru.practicum.validation.UpdateValidationGroup;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AdminCompilationDto {

    private List<Long> events = new ArrayList<>();

    private Boolean pinned = false;

    @NotBlank(groups = CreateValidationGroup.class)
    @Size(max = 50, groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private String title;
}