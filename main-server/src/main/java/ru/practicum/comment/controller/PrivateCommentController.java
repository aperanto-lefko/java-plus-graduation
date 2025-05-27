package ru.practicum.comment.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;
import ru.practicum.validation.CreateValidationGroup;
import ru.practicum.validation.UpdateValidationGroup;

@RestController
@AllArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@PathVariable Long userId,
                                                    @RequestBody
                                                    @Validated(CreateValidationGroup.class) CommentDto commentDto) {
        return ResponseEntity.status(201)
                .body(commentService.createComment(userId, commentDto));
    }

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<CommentDto> createReply(@PathVariable Long userId,
                                                  @PathVariable Long commentId,
                                                  @RequestBody
                                                  @Validated(CreateValidationGroup.class) CommentDto commentDto) {
        return ResponseEntity.status(201)
                .body(commentService.createReply(userId, commentId, commentDto));
    }


    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long userId, @PathVariable Long commentId,
                                                    @RequestBody
                                                    @Validated(UpdateValidationGroup.class) CommentDto commentDto) {
        commentDto.setId(commentId);
        return ResponseEntity.status(200)
                .body(commentService.updateComment(userId, commentDto));
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long userId, @PathVariable Long commentId) {
        return ResponseEntity.status(200)
                .body(commentService.getComment(userId, commentId));
    }
}