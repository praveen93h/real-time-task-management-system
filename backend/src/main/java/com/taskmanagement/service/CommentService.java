package com.taskmanagement.service;

import com.taskmanagement.dto.comment.CommentDTO;
import com.taskmanagement.dto.comment.CreateCommentDTO;
import com.taskmanagement.dto.user.UserDTO;
import com.taskmanagement.entity.Comment;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.ActivityAction;
import com.taskmanagement.enums.EntityType;
import com.taskmanagement.exception.AccessDeniedException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.repository.CommentRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final ProjectService projectService;
    private final AuthService authService;
    private final ActivityLogService activityLogService;
    private final WebSocketEventService webSocketEventService;

    public CommentService(CommentRepository commentRepository,
                         @Lazy TaskService taskService,
                         ProjectService projectService,
                         AuthService authService,
                         @Lazy ActivityLogService activityLogService,
                         @Lazy WebSocketEventService webSocketEventService) {
        this.commentRepository = commentRepository;
        this.taskService = taskService;
        this.projectService = projectService;
        this.authService = authService;
        this.activityLogService = activityLogService;
        this.webSocketEventService = webSocketEventService;
    }

    public List<CommentDTO> getCommentsByTask(Long taskId) {
        User currentUser = authService.getCurrentUser();
        Task task = taskService.getTaskEntity(taskId);
        
        if (!projectService.hasAccess(task.getProject(), currentUser)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
        
        List<Comment> comments = commentRepository.findByTaskIdWithUser(taskId);
        return comments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO addComment(Long taskId, CreateCommentDTO createDTO) {
        User currentUser = authService.getCurrentUser();
        Task task = taskService.getTaskEntity(taskId);
        
        if (!projectService.hasAccess(task.getProject(), currentUser)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
        
        Comment comment = Comment.builder()
                .content(createDTO.getContent())
                .task(task)
                .user(currentUser)
                .build();
        
        comment = commentRepository.save(comment);
        CommentDTO commentDTO = mapToDTO(comment);
        
        activityLogService.logActivity(
                ActivityAction.COMMENTED,
                EntityType.TASK,
                taskId,
                task.getProject().getId(),
                Map.of(
                        "taskTitle", task.getTitle(),
                        "commentPreview", truncate(createDTO.getContent(), 100)
                )
        );
        
        webSocketEventService.notifyCommentAdded(
                task.getProject().getId(), taskId, commentDTO, currentUser.getUsername());
        
        return commentDTO;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = authService.getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        Long taskId = comment.getTask().getId();
        Long projectId = comment.getTask().getProject().getId();
        
        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isOwner = comment.getTask().getProject().getOwner().getId().equals(currentUser.getId());
        
        if (!isAuthor && !isOwner) {
            throw new AccessDeniedException("You don't have permission to delete this comment");
        }
        
        commentRepository.delete(comment);
        
        webSocketEventService.notifyCommentDeleted(projectId, taskId, commentId, currentUser.getUsername());
    }

    public long getCommentCount(Long taskId) {
        return commentRepository.countByTaskId(taskId);
    }

    private CommentDTO mapToDTO(Comment comment) {
        UserDTO userDTO = UserDTO.minimal(
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getUser().getFullName(),
                comment.getUser().getAvatarUrl()
        );
        
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .taskId(comment.getTask().getId())
                .user(userDTO)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}
