package com.taskmanagement.controller;

import com.taskmanagement.dto.task.*;
import com.taskmanagement.enums.TaskStatus;
import com.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByProject(@PathVariable Long projectId) {
        List<TaskDTO> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/projects/{projectId}/tasks/status/{status}")
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status) {
        List<TaskDTO> tasks = taskService.getTasksByProjectAndStatus(projectId, status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/projects/{projectId}/tasks/search")
    public ResponseEntity<List<TaskDTO>> searchTasks(
            @PathVariable Long projectId,
            @RequestParam String query) {
        List<TaskDTO> tasks = taskService.searchTasks(projectId, query);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskDTO> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskDTO createDTO) {
        TaskDTO task = taskService.createTask(projectId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskDTO updateDTO) {
        TaskDTO task = taskService.updateTask(id, updateDTO);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusDTO statusDTO) {
        TaskDTO task = taskService.updateTaskStatus(id, statusDTO);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/tasks/{id}/assign")
    public ResponseEntity<TaskDTO> assignTask(
            @PathVariable Long id,
            @RequestBody AssignTaskDTO assignDTO) {
        TaskDTO task = taskService.assignTask(id, assignDTO);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/tasks/{id}/position")
    public ResponseEntity<TaskDTO> updateTaskPosition(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskPositionDTO positionDTO) {
        TaskDTO task = taskService.updateTaskPosition(id, positionDTO);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Map<String, String>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }

    @GetMapping("/tasks/my-tasks")
    public ResponseEntity<List<TaskDTO>> getMyTasks() {
        List<TaskDTO> tasks = taskService.getMyTasks();
        return ResponseEntity.ok(tasks);
    }
}
