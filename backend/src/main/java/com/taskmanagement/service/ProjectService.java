package com.taskmanagement.service;

import com.taskmanagement.config.RedisConfig.CacheNames;
import com.taskmanagement.dto.project.*;
import com.taskmanagement.dto.user.UserDTO;
import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.ProjectMember;
import com.taskmanagement.entity.ProjectMemberId;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.ProjectRole;
import com.taskmanagement.exception.AccessDeniedException;
import com.taskmanagement.exception.BadRequestException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.repository.ProjectMemberRepository;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;

    public ProjectService(ProjectRepository projectRepository,
                         ProjectMemberRepository projectMemberRepository,
                         UserRepository userRepository,
                         TaskRepository taskRepository,
                         AuthService authService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.authService = authService;
    }

    public List<ProjectDTO> getCurrentUserProjects() {
        User currentUser = authService.getCurrentUser();
        return getProjectsByUserId(currentUser.getId());
    }

    @Cacheable(value = CacheNames.PROJECTS_BY_USER, key = "#userId")
    public List<ProjectDTO> getProjectsByUserId(Long userId) {
        List<Project> projects = projectRepository.findAllProjectsForUser(userId);
        
        return projects.stream()
                .map(this::mapToMinimalDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheNames.PROJECT_BY_ID, key = "#projectId")
    public ProjectDTO getProjectById(Long projectId) {
        User currentUser = authService.getCurrentUser();
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        
        if (!hasAccess(project, currentUser)) {
            throw new AccessDeniedException("You don't have access to this project");
        }
        
        return mapToFullDTO(project);
    }

    @Transactional
    @CacheEvict(value = CacheNames.PROJECTS_BY_USER, allEntries = true)
    public ProjectDTO createProject(CreateProjectDTO createDTO) {
        User currentUser = authService.getCurrentUser();
        
        Project project = Project.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .owner(currentUser)
                .build();
        
        project = projectRepository.save(project);
        
        ProjectMember ownerMember = ProjectMember.builder()
                .id(new ProjectMemberId(project.getId(), currentUser.getId()))
                .project(project)
                .user(currentUser)
                .role(ProjectRole.OWNER)
                .build();
        
        projectMemberRepository.save(ownerMember);
        
        return mapToMinimalDTO(project);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.PROJECT_BY_ID, key = "#projectId"),
        @CacheEvict(value = CacheNames.PROJECTS_BY_USER, allEntries = true)
    })
    public ProjectDTO updateProject(Long projectId, UpdateProjectDTO updateDTO) {
        User currentUser = authService.getCurrentUser();
        Project project = getProjectEntity(projectId);
        
        if (!isOwnerOrAdmin(project, currentUser)) {
            throw new AccessDeniedException("Only project owner or admin can update the project");
        }
        
        if (updateDTO.getName() != null) {
            project.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            project.setDescription(updateDTO.getDescription());
        }
        
        project = projectRepository.save(project);
        return mapToMinimalDTO(project);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.PROJECT_BY_ID, key = "#projectId"),
        @CacheEvict(value = CacheNames.PROJECTS_BY_USER, allEntries = true),
        @CacheEvict(value = CacheNames.PROJECT_MEMBERS, key = "#projectId"),
        @CacheEvict(value = CacheNames.TASKS_BY_PROJECT, key = "#projectId")
    })
    public void deleteProject(Long projectId) {
        User currentUser = authService.getCurrentUser();
        Project project = getProjectEntity(projectId);
        
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only project owner can delete the project");
        }
        
        projectRepository.delete(project);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.PROJECT_MEMBERS, key = "#projectId"),
        @CacheEvict(value = CacheNames.PROJECTS_BY_USER, key = "#addMemberDTO.userId")
    })
    public ProjectMemberDTO addMember(Long projectId, AddMemberDTO addMemberDTO) {
        User currentUser = authService.getCurrentUser();
        Project project = getProjectEntity(projectId);
        
        if (!isOwnerOrAdmin(project, currentUser)) {
            throw new AccessDeniedException("Only project owner or admin can add members");
        }
        
        User userToAdd = userRepository.findById(addMemberDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", addMemberDTO.getUserId()));
        
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, addMemberDTO.getUserId())) {
            throw new BadRequestException("User is already a member of this project");
        }
        
        ProjectRole role = addMemberDTO.getRole();
        if (role == ProjectRole.OWNER) {
            throw new BadRequestException("Cannot add member with OWNER role");
        }
        
        ProjectMember member = ProjectMember.builder()
                .id(new ProjectMemberId(projectId, userToAdd.getId()))
                .project(project)
                .user(userToAdd)
                .role(role)
                .build();
        
        member = projectMemberRepository.save(member);
        
        return mapToMemberDTO(member);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheNames.PROJECT_MEMBERS, key = "#projectId"),
        @CacheEvict(value = CacheNames.PROJECTS_BY_USER, key = "#userId")
    })
    public void removeMember(Long projectId, Long userId) {
        User currentUser = authService.getCurrentUser();
        Project project = getProjectEntity(projectId);
        
        // Check if user is owner or admin (or removing self)
        boolean isSelfRemoval = currentUser.getId().equals(userId);
        if (!isSelfRemoval && !isOwnerOrAdmin(project, currentUser)) {
            throw new AccessDeniedException("Only project owner or admin can remove members");
        }
        
        if (project.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Cannot remove project owner");
        }
        
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("Member not found in project");
        }
        
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Transactional
    @CacheEvict(value = CacheNames.PROJECT_MEMBERS, key = "#projectId")
    public ProjectMemberDTO updateMemberRole(Long projectId, Long userId, UpdateMemberRoleDTO updateDTO) {
        User currentUser = authService.getCurrentUser();
        Project project = getProjectEntity(projectId);
        
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only project owner can change member roles");
        }
        
        if (project.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Cannot change owner's role");
        }
        
        if (updateDTO.getRole() == ProjectRole.OWNER) {
            throw new BadRequestException("Cannot assign OWNER role to members");
        }
        
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in project"));
        
        member.setRole(updateDTO.getRole());
        member = projectMemberRepository.save(member);
        
        return mapToMemberDTO(member);
    }

    @Cacheable(value = CacheNames.PROJECT_MEMBERS, key = "#projectId")
    public List<ProjectMemberDTO> getProjectMembers(Long projectId) {
        User currentUser = authService.getCurrentUser();
        Project project = getProjectEntity(projectId);
        
        if (!hasAccess(project, currentUser)) {
            throw new AccessDeniedException("You don't have access to this project");
        }
        
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return members.stream()
                .map(this::mapToMemberDTO)
                .collect(Collectors.toList());
    }

    public Project getProjectEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }

    public boolean hasAccess(Project project, User user) {
        return project.getOwner().getId().equals(user.getId()) ||
               projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId());
    }

    private boolean isOwnerOrAdmin(Project project, User user) {
        if (project.getOwner().getId().equals(user.getId())) {
            return true;
        }
        
        return projectMemberRepository.findRoleByProjectIdAndUserId(project.getId(), user.getId())
                .map(role -> role == ProjectRole.ADMIN || role == ProjectRole.OWNER)
                .orElse(false);
    }

    private ProjectDTO mapToMinimalDTO(Project project) {
        UserDTO ownerDTO = UserDTO.minimal(
                project.getOwner().getId(),
                project.getOwner().getUsername(),
                project.getOwner().getFullName(),
                project.getOwner().getAvatarUrl()
        );
        
        int taskCount = project.getTasks() != null ? project.getTasks().size() : 0;
        
        return ProjectDTO.minimal(
                project.getId(),
                project.getName(),
                project.getDescription(),
                ownerDTO,
                taskCount,
                project.getCreatedAt()
        );
    }

    private ProjectDTO mapToFullDTO(Project project) {
        UserDTO ownerDTO = UserDTO.minimal(
                project.getOwner().getId(),
                project.getOwner().getUsername(),
                project.getOwner().getFullName(),
                project.getOwner().getAvatarUrl()
        );
        
        List<ProjectMemberDTO> memberDTOs = project.getMembers().stream()
                .map(this::mapToMemberDTO)
                .collect(Collectors.toList());
        
        int taskCount = project.getTasks() != null ? project.getTasks().size() : 0;
        
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .owner(ownerDTO)
                .members(memberDTOs)
                .taskCount(taskCount)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private ProjectMemberDTO mapToMemberDTO(ProjectMember member) {
        UserDTO userDTO = UserDTO.minimal(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getUser().getFullName(),
                member.getUser().getAvatarUrl()
        );
        
        return ProjectMemberDTO.builder()
                .user(userDTO)
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
