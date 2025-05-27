package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.validation.CreateValidationGroup;
import ru.practicum.validation.UpdateValidationGroup;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CommentDto {

    private Long id;

    private UserShortDto user;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created = LocalDateTime.now();

    @NotNull(groups = CreateValidationGroup.class)
    @Positive(groups = CreateValidationGroup.class)
    private Long eventId;

    @NotBlank(groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private String description;

    private Long parentCommentId;

    private List<CommentDto> replies;

}