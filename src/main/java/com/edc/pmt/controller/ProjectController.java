package com.edc.pmt.controller;

import com.edc.pmt.entity.Project;
import com.edc.pmt.entity.ProjectMember;
import com.edc.pmt.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectService.createProject(project);
        return ResponseEntity.ok(savedProject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Project updatedProject) {
        try {
            Project project = projectService.updateProject(id, updatedProject);
            return ResponseEntity.ok(project);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id, @RequestParam Long userId) {
        try {
            projectService.deleteProject(id, userId);
            return ResponseEntity.ok(Map.of("message", "Projet supprimé."));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestParam Long requesterId) {
        try {
            projectService.removeMember(projectId, memberId, requesterId);
            return ResponseEntity.ok(Map.of("message", "Membre supprimé avec succès"));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{projectId}/invite")
    public ResponseEntity<?> inviteMember(
            @PathVariable Long projectId,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam Long requesterId) {
        try {
            projectService.inviteMember(projectId, email, role, requesterId);
            return ResponseEntity.ok(Map.of("message", "Membre invité avec succès"));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<?> getProjectMembers(@PathVariable Long projectId) {
        try {
            List<ProjectMember> members = projectService.getProjectMembers(projectId);
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{projectId}/isMember")
    public ResponseEntity<?> isUserMember(
            @PathVariable Long projectId,
            @RequestParam String email) {
        Map<String, Boolean> result = projectService.isUserMember(projectId, email);
        return ResponseEntity.ok(result);
    }

};