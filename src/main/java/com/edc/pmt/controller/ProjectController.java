package com.edc.pmt.controller;

import com.edc.pmt.entity.Project;
import com.edc.pmt.entity.ProjectMember;
/* import com.edc.pmt.entity.User; */
import com.edc.pmt.repository.ProjectMemberRepository;
import com.edc.pmt.repository.ProjectRepository;
import com.edc.pmt.repository.UserRepository;
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

    // Création de projet avec ajout de l'admin
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectRepository.save(project);
        userRepository.findById(savedProject.getCreateBy()).ifPresent(creator -> {
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProject(savedProject);
            projectMember.setUser(creator);
            projectMember.setRole("ADMIN");
            projectMemberRepository.save(projectMember);
        });
        return ResponseEntity.ok(savedProject);
    }

    // Suppression par le créateur
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id, @RequestParam Long userId) {
        return projectRepository.findById(id).map(project -> {
            if (!project.getCreateBy().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorisé à supprimer ce projet.");
            }
            projectRepository.delete(project);
            return ResponseEntity.ok(Map.of("message", "Projet supprimé."));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Modification de projet (par le créateur uniquement)
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

    // Retirer un membre (admin uniquement)
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestParam Long requesterId) {
        var projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty() || !projectOpt.get().getCreateBy().equals(requesterId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Permission refusée");
        }
        var memberOpt = projectMemberRepository.findById(memberId);
        if (memberOpt.isEmpty() || !memberOpt.get().getProject().getId().equals(projectId)) {
            return ResponseEntity.badRequest().body("Membre introuvable ou n'appartient pas à ce projet");
        }
        projectMemberRepository.delete(memberOpt.get());
        return ResponseEntity.ok(Map.of("message", "Membre supprimé avec succès"));
    }

    // Inviter un membre (admin uniquement)
    @PostMapping("/{projectId}/invite")
    public ResponseEntity<?> inviteMember(
            @PathVariable Long projectId,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam Long requesterId) {
        var projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty() || !projectOpt.get().getCreateBy().equals(requesterId)) {
            return ResponseEntity.status(403).body("Permission refusée");
        }
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur introuvable");
        }
        boolean isAlreadyMember = projectMemberRepository
                .findByProjectId(projectId)
                .stream()
                .anyMatch(pm -> pm.getUser().getId().equals(userOpt.get().getId()));
        if (isAlreadyMember) {
            return ResponseEntity.badRequest().body("Utilisateur déjà membre du projet");
        }
        ProjectMember pm = new ProjectMember();
        pm.setProject(projectOpt.get());
        pm.setUser(userOpt.get());
        pm.setRole(role);
        projectMemberRepository.save(pm);
        return ResponseEntity.ok(Map.of("message", "Membre invité avec succès"));
    }

    // Liste des membres d'un projet
    @GetMapping("/{projectId}/members")
    public ResponseEntity<?> getProjectMembers(@PathVariable Long projectId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return ResponseEntity.ok(members);
    }

    // Vérifier si un user est membre d'un projet
    @GetMapping("/{projectId}/isMember")
    public ResponseEntity<?> isUserMember(
            @PathVariable Long projectId,
            @RequestParam String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("exists", false, "isMember", false));
        }
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userOpt.get().getId());
        return ResponseEntity.ok(Map.of("exists", true, "isMember", isMember));
    }
}
