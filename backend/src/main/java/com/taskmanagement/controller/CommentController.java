package com.taskmanagement.controller;

import com.taskmanagement.dto.comment.CommentDTO;
import com.taskmanagement.dto.comment.CreateCommentDTO;
import com.taskmanagement.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getTaskComments(@PathVariable Long taskId) {
        List<CommentDTO> comments = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentDTO createDTO) {
        CommentDTO comment = commentService.addComment(taskId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long taskId) {
        long count = commentService.getCommentCount(taskId);
        return ResponseEntity.ok(count);
    }
}
