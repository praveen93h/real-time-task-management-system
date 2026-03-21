package com.taskmanagement.controller;

import com.taskmanagement.dto.project.*;
import com.taskmanagement.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<ProjectDTO> projects = projectService.getCurrentUserProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        ProjectDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody CreateProjectDTO createDTO) {
        ProjectDTO project = projectService.createProject(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectDTO updateDTO) {
        ProjectDTO project = projectService.updateProject(id, updateDTO);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ProjectMemberDTO>> getProjectMembers(@PathVariable Long id) {
        List<ProjectMemberDTO> members = projectService.getProjectMembers(id);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectMemberDTO> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberDTO addMemberDTO) {
        ProjectMemberDTO member = projectService.addMember(id, addMemberDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @PatchMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectMemberDTO> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleDTO updateDTO) {
        ProjectMemberDTO member = projectService.updateMemberRole(projectId, userId, updateDTO);
        return ResponseEntity.ok(member);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        projectService.removeMember(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }
}
