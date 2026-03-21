package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import com.taskmanagement.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectIdOrderByPositionAsc(Long projectId);

    List<Task> findByProjectIdAndStatusOrderByPositionAsc(Long projectId, TaskStatus status);

    List<Task> findByAssignedToId(Long userId);

    List<Task> findByCreatedById(Long userId);

    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.project " +
           "LEFT JOIN FETCH t.assignedTo " +
           "LEFT JOIN FETCH t.createdBy " +
           "WHERE t.id = :taskId")
    Optional<Task> findByIdWithDetails(@Param("taskId") Long taskId);

    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.assignedTo " +
           "LEFT JOIN FETCH t.createdBy " +
           "WHERE t.project.id = :projectId " +
           "ORDER BY t.position ASC")
    List<Task> findByProjectIdWithDetails(@Param("projectId") Long projectId);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :today AND :endDate AND t.status != 'DONE'")
    List<Task> findTasksDueSoon(@Param("today") LocalDate today, @Param("endDate") LocalDate endDate);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> countTasksByStatusForProject(@Param("projectId") Long projectId);

    @Modifying
    @Query("UPDATE Task t SET t.position = :position WHERE t.id = :taskId")
    void updatePosition(@Param("taskId") Long taskId, @Param("position") Integer position);

    @Modifying
    @Query("UPDATE Task t SET t.status = :status WHERE t.id = :taskId")
    void updateStatus(@Param("taskId") Long taskId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Task> searchTasksInProject(@Param("projectId") Long projectId, @Param("query") String query);
}
