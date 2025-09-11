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

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectRepository.save(project);
        // Créer le membre administrateur à partir du créateur
        User creator = userRepository.findById(savedProject.getCreateBy()).orElse(null);

        if (creator != null) {
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProject(savedProject);
            projectMember.setUser(creator);
            projectMember.setRole("ADMIN");
            projectMemberRepository.save(projectMember);
        }

        return ResponseEntity.ok(savedProject);
    }

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

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestParam Long requesterId) {
        var projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Projet introuvable");
        }
        Project project = projectOpt.get();

        if (!project.getCreateBy().equals(requesterId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Permission refusée");
        }

        var memberOpt = projectMemberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Membre introuvable");
        }

        ProjectMember member = memberOpt.get();
        if (!member.getProject().getId().equals(projectId)) {
            return ResponseEntity.badRequest().body("Le membre n’appartient pas à ce projet");
        }

        projectMemberRepository.delete(member);
        return ResponseEntity.ok(Map.of("message", "Membre supprimé avec succès"));
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

        // Vérification si l'utilisateur est déjà dans les membres du projet
        boolean isAlreadyMember = projectMemberRepository
                .findByProjectId(projectId)
                .stream()
                .anyMatch(pm -> pm.getUser().getId().equals(user.getId()));

        if (isAlreadyMember) {
            return ResponseEntity.badRequest().body("Utilisateur déjà membre du projet");
        }

        ProjectMember pm = new ProjectMember();
        pm.setProject(project);
        pm.setUser(user);
        pm.setRole(role);
        projectMemberRepository.save(pm);

        return ResponseEntity.ok(Map.of("message", "Membre invité avec succès"));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<?> getProjectMembers(@PathVariable Long projectId) {
        var projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);

        return ResponseEntity.ok(members);
    }

    @GetMapping("/{projectId}/isMember")
    public ResponseEntity<?> isUserMember(
            @PathVariable Long projectId,
            @RequestParam String email) {

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("exists", false, "isMember", false));
        }
        User user = userOpt.get();
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId());
        return ResponseEntity.ok(Map.of("exists", true, "isMember", isMember));
    }
};
