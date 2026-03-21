package com.taskmanagement.entity;

import com.taskmanagement.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {

    @EmbeddedId
    private ProjectMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ProjectRole role = ProjectRole.MEMBER;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    // Convenience constructor
    public ProjectMember(Project project, User user, ProjectRole role) {
        this.project = project;
        this.user = user;
        this.role = role;
        this.id = new ProjectMemberId(project.getId(), user.getId());
    }
}
