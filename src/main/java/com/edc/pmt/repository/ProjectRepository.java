package com.edc.pmt.repository;

import com.edc.pmt.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Accès aux projets.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
};