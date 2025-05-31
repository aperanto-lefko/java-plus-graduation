package ru.practicum.compilation.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.AdminCompilationDto;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.validation.CreateValidationGroup;
import ru.practicum.validation.UpdateValidationGroup;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> addCompilation(@RequestBody @Validated(CreateValidationGroup.class)
                                                         AdminCompilationDto adminCompilationDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compilationService.create(adminCompilationDto));
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(@RequestBody @Validated(UpdateValidationGroup.class)
                                                            AdminCompilationDto adminCompilationDto,
                                                            @PathVariable Long compId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationService.update(adminCompilationDto, compId));
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compilationService.delete(compId);
    }
}