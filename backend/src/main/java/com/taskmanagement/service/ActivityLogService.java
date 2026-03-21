package com.taskmanagement.service;

import com.taskmanagement.dto.activity.ActivityLogDTO;
import com.taskmanagement.dto.user.UserDTO;
import com.taskmanagement.entity.ActivityLog;
import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.ActivityAction;
import com.taskmanagement.enums.EntityType;
import com.taskmanagement.exception.AccessDeniedException;
import com.taskmanagement.repository.ActivityLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectService projectService;
    private final AuthService authService;

    public ActivityLogService(ActivityLogRepository activityLogRepository,
                             ProjectService projectService,
                             AuthService authService) {
        this.activityLogRepository = activityLogRepository;
        this.projectService = projectService;
        this.authService = authService;
    }

    @Transactional
    public void logActivity(ActivityAction action, EntityType entityType, 
            Long entityId, Long projectId, Map<String, Object> details) {
        User currentUser = authService.getCurrentUser();
        
        ActivityLog log = ActivityLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .user(currentUser)
                .projectId(projectId)
                .details(details)
                .build();
        
        activityLogRepository.save(log);
    }

    public List<ActivityLogDTO> getProjectActivities(Long projectId, int limit) {
        User currentUser = authService.getCurrentUser();
        Project project = projectService.getProjectEntity(projectId);
        
        if (!projectService.hasAccess(project, currentUser)) {
            throw new AccessDeniedException("You don't have access to this project");
        }
        
        List<ActivityLog> logs = activityLogRepository.findRecentByProjectId(
                projectId, PageRequest.of(0, limit));
        
        return logs.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ActivityLogDTO> getEntityActivities(EntityType entityType, Long entityId) {
        User currentUser = authService.getCurrentUser();
        
        List<ActivityLog> logs = activityLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
        
        // Access check based on entity type
        if (!logs.isEmpty()) {
            Long projectId = logs.get(0).getProjectId();
            if (projectId != null) {
                Project project = projectService.getProjectEntity(projectId);
                if (!projectService.hasAccess(project, currentUser)) {
                    throw new AccessDeniedException("You don't have access to this resource");
                }
            }
        }
        
        return logs.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ActivityLogDTO mapToDTO(ActivityLog log) {
        UserDTO userDTO = UserDTO.minimal(
                log.getUser().getId(),
                log.getUser().getUsername(),
                log.getUser().getFullName(),
                log.getUser().getAvatarUrl()
        );
        
        return ActivityLogDTO.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .user(userDTO)
                .projectId(log.getProjectId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .description(generateDescription(log))
                .build();
    }

    private String generateDescription(ActivityLog log) {
        String userName = log.getUser().getFullName() != null ? 
                log.getUser().getFullName() : log.getUser().getUsername();
        
        String entityName = "";
        if (log.getDetails() != null) {
            if (log.getDetails().containsKey("taskTitle")) {
                entityName = (String) log.getDetails().get("taskTitle");
            } else if (log.getDetails().containsKey("projectName")) {
                entityName = (String) log.getDetails().get("projectName");
            }
        }
        
        return switch (log.getAction()) {
            case CREATED -> String.format("%s created %s '%s'", 
                    userName, log.getEntityType().name().toLowerCase(), entityName);
            case UPDATED -> String.format("%s updated %s '%s'", 
                    userName, log.getEntityType().name().toLowerCase(), entityName);
            case DELETED -> String.format("%s deleted %s '%s'", 
                    userName, log.getEntityType().name().toLowerCase(), entityName);
            case ASSIGNED -> String.format("%s assigned task '%s'", userName, entityName);
            case UNASSIGNED -> String.format("%s unassigned task '%s'", userName, entityName);
            case STATUS_CHANGED -> {
                String newStatus = log.getDetails() != null ? 
                        (String) log.getDetails().get("newStatus") : "";
                yield String.format("%s changed status of '%s' to %s", 
                        userName, entityName, newStatus);
            }
            case COMMENTED -> String.format("%s commented on '%s'", userName, entityName);
            case MEMBER_ADDED -> {
                String memberName = log.getDetails() != null ? 
                        (String) log.getDetails().get("memberName") : "";
                yield String.format("%s added %s to the project", userName, memberName);
            }
            case MEMBER_REMOVED -> {
                String memberName = log.getDetails() != null ? 
                        (String) log.getDetails().get("memberName") : "";
                yield String.format("%s removed %s from the project", userName, memberName);
            }
        };
    }
}
