package com.edc.pmt.repository;

import com.edc.pmt.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Accès aux tâches.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
};