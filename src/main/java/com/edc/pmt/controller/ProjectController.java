package com.edc.pmt.controller;

import com.edc.pmt.entity.Project;
import com.edc.pmt.entity.ProjectMember;
import com.edc.pmt.entity.User;
import com.edc.pmt.repository.ProjectMemberRepository;
import com.edc.pmt.repository.ProjectRepository;
import com.edc.pmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectRepository.save(project);
        return ResponseEntity.ok(savedProject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Project updatedProject) {
        return projectRepository.findById(id).map(project -> {
            if (!project.getCreateBy().equals(updatedProject.getCreateBy())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorisé à modifier ce projet.");
            }
            project.setName(updatedProject.getName());
            project.setDescription(updatedProject.getDescription());
            project.setStartDate(updatedProject.getStartDate());
            projectRepository.save(project);
            return ResponseEntity.ok(project);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id, @RequestParam Long userId) {
        return projectRepository.findById(id).map(project -> {
            if (!project.getCreateBy().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorisé à supprimer ce projet.");
            }
            projectRepository.delete(project);
            return ResponseEntity.ok().body("Projet supprimé.");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{projectId}/invite")
    public ResponseEntity<?> inviteMember(
            @PathVariable Long projectId,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam Long requesterId) {
        var projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty())
            return ResponseEntity.badRequest().body("Projet introuvable");
        Project project = projectOpt.get();

        if (!project.getCreateBy().equals(requesterId)) {
            return ResponseEntity.status(403).body("Permission refusée");
        }

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.badRequest().body("Utilisateur introuvable");
        User user = userOpt.get();

        ProjectMember pm = new ProjectMember();
        pm.setProject(project);
        pm.setUser(user);
        pm.setRole(role);
        projectMemberRepository.save(pm);

        return ResponseEntity.ok("Membre invité avec succès");
    }
}
