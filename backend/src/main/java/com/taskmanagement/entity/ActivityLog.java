package com.taskmanagement.entity;

import com.taskmanagement.enums.ActivityAction;
import com.taskmanagement.enums.EntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_activity_entity", columnList = "entity_id, entity_type"),
    @Index(name = "idx_activity_project", columnList = "project_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "project_id")
    private Long projectId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
