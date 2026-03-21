package com.taskmanagement.repository;

import com.taskmanagement.entity.ProjectMember;
import com.taskmanagement.entity.ProjectMemberId;
import com.taskmanagement.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user WHERE pm.project.id = :projectId")
    List<ProjectMember> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.project WHERE pm.user.id = :userId")
    List<ProjectMember> findByUserId(@Param("userId") Long userId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    @Modifying
    @Query("DELETE FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    void deleteByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Query("SELECT pm.role FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    Optional<ProjectRole> findRoleByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    long countByProjectId(Long projectId);
}
