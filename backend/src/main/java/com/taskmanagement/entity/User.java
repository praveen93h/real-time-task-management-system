package com.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Projects owned by this user
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Project> ownedProjects = new ArrayList<>();

    // Project memberships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectMember> projectMemberships = new ArrayList<>();

    // Tasks assigned to this user
    @OneToMany(mappedBy = "assignedTo")
    @Builder.Default
    private List<Task> assignedTasks = new ArrayList<>();

    // Tasks created by this user
    @OneToMany(mappedBy = "createdBy")
    @Builder.Default
    private List<Task> createdTasks = new ArrayList<>();

    // Comments made by this user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
}
