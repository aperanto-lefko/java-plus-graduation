package ru.practicum.comment.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@RestController
@AllArgsConstructor
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long eventId) {
        return ResponseEntity.status(200)
                .body(commentService.getComments(eventId));
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<CommentDto>> getReplies(@PathVariable Long commentId) {
        return ResponseEntity.status(200)
                .body(commentService.getReplies(commentId));
    }
}