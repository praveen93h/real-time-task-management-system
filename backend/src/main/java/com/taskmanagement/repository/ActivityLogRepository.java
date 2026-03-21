package com.taskmanagement.repository;

import com.taskmanagement.entity.ActivityLog;
import com.taskmanagement.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType entityType, Long entityId);

    Page<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    @Query("SELECT a FROM ActivityLog a " +
           "JOIN FETCH a.user " +
           "WHERE a.projectId = :projectId " +
           "ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT a FROM ActivityLog a WHERE a.projectId = :projectId " +
           "AND a.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY a.createdAt DESC")
    List<ActivityLog> findByProjectIdAndDateRange(
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    void deleteByCreatedAtBefore(LocalDateTime date);

    long countByProjectId(Long projectId);
}
