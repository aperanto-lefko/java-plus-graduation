package ru.practicum.comment.service;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.feign.EventServiceClient;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.feign.UserServiceClient;


import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventServiceClient eventServiceClient;
    private final UserServiceClient userServiceClient;

    @Override
    public List<CommentDto> getComments(Long eventId) {
        log.info("Get comments by eventId: {}", eventId);
        EventFullDto eventFullDto;
        try {
            eventFullDto = eventServiceClient.getEventById(eventId);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException(
                    String.format("Не найдено события с id: %d", eventId)
            );
        }

        List<CommentDto> dtos = CommentMapper.INSTANCE.toDtos(commentRepository.findAllByEventId(eventFullDto.getId()));
        return dtos.stream()
                // Избегаем дублирования комментариев в ответе
                .filter((dto) -> dto.getParentCommentId() == null)
                .toList();
    }

    @Override
    public CommentDto createComment(Long userId, CommentDto commentDto) {
        UserShortDto user = getUserById(userId);
        EventFullDto eventFullDto = eventServiceClient.getEventById(commentDto.getEventId());
//        Event event = eventService.getPublicEventById(commentDto.getEventId());
        Comment comment = CommentMapper.INSTANCE.toEntity(commentDto);
        comment.setUserId(user.getId());

        comment.setEventId(eventFullDto.getId());

        log.info("Create comment: {}", comment);
        CommentDto savedCommentDto=  CommentMapper.INSTANCE.toDto(commentRepository.save(comment));
        savedCommentDto.setUser(user);
        return savedCommentDto;
    }

    @Override
    public CommentDto updateComment(CommentDto commentDto) {
//        Event event = eventService.getPublicEventById(commentDto.getEventId());
        EventFullDto eventFullDto = eventServiceClient.getEventById(commentDto.getEventId());
        Comment comment = getCommentById(commentDto.getId());
        CommentMapper.INSTANCE.updateDto(commentDto, comment);
        log.info("Admin update comment: {}", comment);
        //
        comment.setEventId(eventFullDto.getId());

        return CommentMapper.INSTANCE.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateComment(Long userId, CommentDto commentDto) {
        UserShortDto user = getUserById(userId);
//        Event event = eventService.getPublicEventById(commentDto.getEventId());
        EventFullDto eventFullDto = eventServiceClient.getEventById(commentDto.getEventId());
        Comment comment = getCommentById(commentDto.getId());
        CommentMapper.INSTANCE.updateDto(commentDto, comment);
        log.info("Update comment: {}", comment);
        comment.setUserId(userId);
        //
        comment.setEventId(eventFullDto.getId());

        return CommentMapper.INSTANCE.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        getUserById(userId);
        if (commentRepository.existsById(commentId)) {
            log.info("Delete comment: {}", commentId);
            commentRepository.deleteById(commentId);
        } else {
            throw new NotFoundException("Comment not found by id: " + commentId);
        }
    }

    @Override
    public void deleteComment(Long commentId) {
        if (commentRepository.existsById(commentId)) {
            log.info("Admin delete comment: {}", commentId);
            commentRepository.deleteById(commentId);
        } else {
            throw new NotFoundException("Comment not found by id: " + commentId);
        }
    }

    @Override
    public CommentDto getComment(Long userId, Long commentId) {
        getUserById(userId);
        log.info("Get comment by id: {}", commentId);
        return CommentMapper.INSTANCE.toDto(getCommentById(commentId));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Не найден комментарий с id: " + commentId));
    }

    @Override
    public CommentDto createReply(Long userId, Long parentCommentId, CommentDto commentDto) {
        UserShortDto user = getUserById(userId);
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found: " + parentCommentId));

        Comment comment = CommentMapper.INSTANCE.toEntity(commentDto);
        comment.setUserId(userId);
        //
        comment.setEventId(parentComment.getEventId());

        comment.setParentComment(parentComment);

        log.info("Create reply to comment: {}", comment);
        return CommentMapper.INSTANCE.toDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getReplies(Long commentId) {
        log.info("Get replies for commentId: {}", commentId);
        return CommentMapper.INSTANCE.toDtos(commentRepository.findAllByParentCommentId(commentId));
    }
    private UserShortDto getUserById(Long userId) {
        UserShortDto user = userServiceClient.getUserById(userId).getBody();
        if (user == null) {
            throw new NotFoundException("Пользователь не найден с id: " + userId);
        }
        return user;
    }
   }