package com.taskmanagement.repository;

import com.taskmanagement.entity.Project;
import com.taskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(User owner);

    List<Project> findByOwnerId(Long ownerId);

    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.members m " +
           "WHERE p.owner.id = :userId OR m.user.id = :userId")
    List<Project> findAllProjectsForUser(@Param("userId") Long userId);

    @Query("SELECT p FROM Project p " +
           "LEFT JOIN FETCH p.members m " +
           "LEFT JOIN FETCH m.user " +
           "WHERE p.id = :projectId")
    Optional<Project> findByIdWithMembers(@Param("projectId") Long projectId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p " +
           "LEFT JOIN p.members m " +
           "WHERE p.id = :projectId AND (p.owner.id = :userId OR m.user.id = :userId)")
    boolean isUserMemberOfProject(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
