package com.taskmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberId implements Serializable {

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;
}
