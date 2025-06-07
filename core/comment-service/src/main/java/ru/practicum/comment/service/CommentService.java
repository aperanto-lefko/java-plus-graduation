package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;

import java.util.List;

public interface CommentService {

    List<CommentDto> getComments(Long eventId);

    CommentDto getComment(Long userId, Long commentId);

    CommentDto createComment(Long userId, CommentDto commentDto);

    CommentDto createReply(Long userId, Long parentCommentId, CommentDto commentDto);

    CommentDto updateComment(CommentDto commentDto);

    CommentDto updateComment(Long userId, CommentDto commentDto);

    void deleteComment(Long userId, Long commentId);

    void deleteComment(Long commentId);

    List<CommentDto> getReplies(Long commentId);

    }
