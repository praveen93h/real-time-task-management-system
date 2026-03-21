package com.taskmanagement.controller;

import com.taskmanagement.dto.activity.ActivityLogDTO;
import com.taskmanagement.enums.EntityType;
import com.taskmanagement.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    // Get project activity log
    @GetMapping("/projects/{projectId}/activities")
    public ResponseEntity<List<ActivityLogDTO>> getProjectActivities(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "50") int limit) {
        List<ActivityLogDTO> activities = activityLogService.getProjectActivities(projectId, limit);
        return ResponseEntity.ok(activities);
    }

    // Get activities for a task
    @GetMapping("/tasks/{taskId}/activities")
    public ResponseEntity<List<ActivityLogDTO>> getTaskActivities(@PathVariable Long taskId) {
        List<ActivityLogDTO> activities = activityLogService.getEntityActivities(EntityType.TASK, taskId);
        return ResponseEntity.ok(activities);
    }
}
