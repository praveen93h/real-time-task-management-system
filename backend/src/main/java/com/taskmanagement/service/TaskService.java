package com.taskmanagement.service;

import com.taskmanagement.config.RedisConfig.CacheNames;
import com.taskmanagement.dto.task.*;
import com.taskmanagement.dto.user.UserDTO;
import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.TaskStatus;
import com.taskmanagement.exception.AccessDeniedException;
import com.taskmanagement.exception.BadRequestException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.repository.CommentRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ProjectService projectService;
    private final AuthService authService;
    private final WebSocketEventService webSocketEventService;

    public TaskService(TaskRepository taskRepository,
                      UserRepository userRepository,
                      CommentRepository commentRepository,
                      ProjectService projectService,
                      AuthService authService,
                      @Lazy WebSocketEventService webSocketEventService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.projectService = projectService;
        this.authService = authService;
        this.webSocketEventService = webSocketEventService;
    }

    @Cacheable(value = CacheNames.TASKS_BY_PROJECT, key = "#projectId")
    public List<TaskDTO> getTasksByProject(Long projectId) {
        User currentUser = authService.getCurrentUser();
        Project project = projectService.getProjectEntity(projectId);
        
        if (!projectService.hasAccess(project, currentUser)) {
            throw new AccessDeniedException("You don't have access to this project");
        }
        
        List<Task> tasks = taskRepository.findByProjectIdWithDetails(projectId);
        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByProjectAndStatus(Long projectId, TaskStatus status) {
        User currentUser = authService.getCurrentUser();
        Project project = projectService.getProjectEntity(projectId);
        
        if (!projectService.hasAccess(project, currentUser)) {
            throw new AccessDeniedException("You don't have access to this project");
        }
        
        List<Task> tasks = taskRepository.findByProjectIdAndStatusOrderByPositionAsc(projectId, status);
        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheNames.TASK_BY_ID, key = "#taskId")
    public TaskDTO getTaskById(Long taskId) {
        User currentUser = authService.getCurrentUser();
        Task task = getTaskEntity(taskId);
        
        if (!projectService.hasAccess(task.getProject(), currentUser)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
        
        return mapToDTO(task);
    }

    @Transactional
    @CacheEvict(value = CacheNames.TASKS_BY_PROJECT, key = "#projectId")
    public TaskDTO createTask(Long projectId, CreateTaskDTO createDTO) {
        User currentUser = authService.getCurrentUser();
        Project project = projectService.getProjectEntity(projectId);
        
        if (!projectService.hasAccess(project, currentUser)) {
            throw new AccessDeniedException("You don't have access to this project");
        }
        
        Task task = Task.builder()
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .status(createDTO.getStatus() != null ? createDTO.getStatus() : TaskStatus.TODO)
                .priority(createDTO.getPriority())
                .project(project)
                .createdBy(currentUser)
                .dueDate(createDTO.getDueDate())
                .position(0)
                .build();
        
        if (createDTO.getAssignedToId() != null) {
            User assignee = userRepository.findById(createDTO.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDTO.getAssignedToId()));
            
            if (!projectService.hasAccess(project, assignee)) {
                throw new BadRequestException("User is not a member of this project");
            }
            task.setAssignedTo(assignee);
        }
        
        task = taskRepository.save(task);
        TaskDTO taskDTO = mapToDTO(task);
        
        webSocketEventService.notifyTaskCreated(projectId, task.getId(), taskDTO, currentUser.getUsername());
        
        return taskDTO;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.TASK_BY_ID, key = "#taskId"),
        @CacheEvict(value = CacheNames.TASKS_BY_PROJECT, allEntries = true)
    })
    public TaskDTO updateTask(Long taskId, UpdateTaskDTO updateDTO) {
        User currentUser = authService.getCurrentUser();
        Task task = getTaskEntity(taskId);
        
        if (!projectService.hasAccess(task.getProject(), currentUser)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
        
        if (updateDTO.getTitle() != null) {
            task.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null) {
            task.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getStatus() != null) {
            task.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getPriority() != null) {
            task.setPriority(updateDTO.getPriority());
        }
        if (updateDTO.getDueDate() != null) {
            task.setDueDate(updateDTO.getDueDate());
        }
        if (updateDTO.getPosition() != null) {
            task.setPosition(updateDTO.getPosition());
        }
        
        if (updateDTO.getAssignedToId() != null) {
            User assignee = userRepository.findById(updateDTO.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", updateDTO.getAssignedToId()));
            
            if (!projectService.hasAccess(task.getProject(), assignee)) {
                throw new BadRequestException("User is not a member of this project");
            }
            task.setAssignedTo(assignee);
        }
        
        task = taskRepository.save(task);
        TaskDTO taskDTO = mapToDTO(task);
        
        webSocketEventService.notifyTaskUpdated(task.getProject().getId(), task.getId(), taskDTO, currentUser.getUsername());
        
        return taskDTO;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.TASK_BY_ID, key = "#taskId"),
        @CacheEvict(value = CacheNames.TASKS_BY_PROJECT, allEntries = true)
    })
    public TaskDTO updateTaskStatus(Long taskId, UpdateTaskStatusDTO statusDTO) {
        User currentUser = authService.getCurrentUser();
        Task task = getTaskEntity(taskId);
        
        if (!projectService.hasAccess(task.getProject(), currentUser)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
        
        task.setStatus(statusDTO.getStatus());
        if (statusDTO.getPosition() != null) {
            task.setPosition(statusDTO.getPosition());
        }
        
        task = taskRepository.save(task);
        TaskDTO taskDTO = mapToDTO(task);
        
        webSocketEventService.notifyTaskStatusChanged(task.getProject().getId(), task.getId(), taskDTO, currentUser.getUsername());
        
        return taskDTO;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.TASK_BY_ID, key = "#taskId"),
        @CacheEvict(value = CacheNames.TASKS_BY_PROJECT, allEntries = true)
    })
    public TaskDTO assignTask(Long taskId, AssignTaskDTO assignDTO) {
        User currentUser = authService.getCurrentUser();
        Task task = getTaskEntity(taskId);
        
        if (!projectService.hasAccess(task.getProject(), currentUser)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
        
        if (assignDTO.getUserId() == null) {
            task.setAssignedTo(null);
        } else {
            User assignee = userRepository.findById(assignDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignDTO.getUserId()));
            
            if (!projectService.hasAccess(task.getProject(), assignee)) {
                throw new BadRequestException("User is not a member of this project");
            }
            task.setAssignedTo(assignee);
        }
        
        task = taskRepository.save(task);
        TaskDTO taskDTO = mapToDTO(task);
        
        webSocketEventService.notifyTaskAssigned(task.getProject().getId(), task.getId(), taskDTO, currentUser.getUsername());
        
        return taskDTO;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.TASK_BY_ID, key = "#taskId"),
        @CacheEvict(value = CacheNames.TASKS_BY_PROJECT, allEntries = true)
    })
    public TaskDTO updateTaskPosition(Long taskId, UpdateTaskPositionDTO positionDTO) {
        User currentUser = authService.getCurrentUser();
        Task task = getTaskEntity(taskId);
        
        if (!projectService.hasAccess(task.getProject(), currentUser)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
        
        task.setPosition(positionDTO.getPosition());
        task = taskRepository.save(task);
        TaskDTO taskDTO = mapToDTO(task);
        
        webSocketEventService.notifyTaskPositionChanged(task.getProject().getId(), task.getId(), taskDTO, currentUser.getUsername());
        
        return taskDTO;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.TASK_BY_ID, key = "#taskId"),
        @CacheEvict(value = CacheNames.TASKS_BY_PROJECT, allEntries = true)
    })
    public void deleteTask(Long taskId) {
        User currentUser = authService.getCurrentUser();
        Task task = getTaskEntity(taskId);
        Long projectId = task.getProject().getId();
        
        if (!projectService.hasAccess(task.getProject(), currentUser)) {
            throw new AccessDeniedException("You don't have access to this task");
        }
        
        taskRepository.delete(task);
        
        webSocketEventService.notifyTaskDeleted(projectId, taskId, currentUser.getUsername());
    }

    public List<TaskDTO> searchTasks(Long projectId, String query) {
        User currentUser = authService.getCurrentUser();
        Project project = projectService.getProjectEntity(projectId);
        
        if (!projectService.hasAccess(project, currentUser)) {
            throw new AccessDeniedException("You don't have access to this project");
        }
        
        List<Task> tasks = taskRepository.searchTasksInProject(projectId, query);
        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getMyTasks() {
        User currentUser = authService.getCurrentUser();
        List<Task> tasks = taskRepository.findByAssignedToId(currentUser.getId());
        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Task getTaskEntity(Long taskId) {
        return taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
    }

    private TaskDTO mapToDTO(Task task) {
        UserDTO assignedToDTO = null;
        if (task.getAssignedTo() != null) {
            assignedToDTO = UserDTO.minimal(
                    task.getAssignedTo().getId(),
                    task.getAssignedTo().getUsername(),
                    task.getAssignedTo().getFullName(),
                    task.getAssignedTo().getAvatarUrl()
            );
        }
        
        UserDTO createdByDTO = null;
        if (task.getCreatedBy() != null) {
            createdByDTO = UserDTO.minimal(
                    task.getCreatedBy().getId(),
                    task.getCreatedBy().getUsername(),
                    task.getCreatedBy().getFullName(),
                    task.getCreatedBy().getAvatarUrl()
            );
        }
        
        long commentCount = commentRepository.countByTaskId(task.getId());
        
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .projectId(task.getProject().getId())
                .assignedTo(assignedToDTO)
                .createdBy(createdByDTO)
                .dueDate(task.getDueDate())
                .position(task.getPosition())
                .commentCount((int) commentCount)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
