package com.edc.pmt.service;

import com.edc.pmt.entity.Project;
import com.edc.pmt.entity.ProjectMember;
import com.edc.pmt.entity.Role;
import com.edc.pmt.entity.User;
import com.edc.pmt.repository.ProjectMemberRepository;
import com.edc.pmt.repository.ProjectRepository;
import com.edc.pmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public Project createProject(Project project) {
        Project savedProject = projectRepository.save(project);
        Optional<User> creatorOpt = userRepository.findById(savedProject.getCreateBy());
        creatorOpt.ifPresent(creator -> {
            ProjectMember member = new ProjectMember();
            member.setProject(savedProject);
            member.setUser(creator);
            member.setRole(Role.ADMIN); // Set with enum
            projectMemberRepository.save(member);
        });
        return savedProject;
    }

    public Project updateProject(Long id, Project updatedProject) throws IllegalAccessException {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projet non trouvé"));
        if (!project.getCreateBy().equals(updatedProject.getCreateBy())) {
            throw new IllegalAccessException("Non autorisé à modifier ce projet");
        }
        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
        project.setStartDate(updatedProject.getStartDate());
        return projectRepository.save(project);
    }

    public void deleteProject(Long projectId, Long userId) throws IllegalAccessException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Projet non trouvé"));
        if (!project.getCreateBy().equals(userId)) {
            throw new IllegalAccessException("Non autorisé à supprimer ce projet");
        }
        projectRepository.delete(project);
    }

    public void removeMember(Long projectId, Long memberId, Long requesterId)
            throws IllegalAccessException, IllegalArgumentException {
        var projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty() || !projectOpt.get().getCreateBy().equals(requesterId)) {
            throw new IllegalAccessException("Permission refusée");
        }
        var memberOpt = projectMemberRepository.findById(memberId);
        if (memberOpt.isEmpty() || !memberOpt.get().getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Membre introuvable ou n'appartient pas à ce projet");
        }
        projectMemberRepository.delete(memberOpt.get());
    }

    public void inviteMember(Long projectId, String email, String role, Long requesterId)
            throws IllegalAccessException, IllegalArgumentException {
        var projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty() || !projectOpt.get().getCreateBy().equals(requesterId)) {
            throw new IllegalAccessException("Permission refusée");
        }
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur introuvable");
        }
        boolean isAlreadyMember = projectMemberRepository
                .findByProjectId(projectId)
                .stream()
                .anyMatch(pm -> pm.getUser().getId().equals(userOpt.get().getId()));
        if (isAlreadyMember) {
            throw new IllegalArgumentException("Utilisateur déjà membre du projet");
        }
        ProjectMember pm = new ProjectMember();
        pm.setProject(projectOpt.get());
        pm.setUser(userOpt.get());

        try {
            pm.setRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rôle invalide. Rôles valides : ADMIN, MEMBER, OBSERVER");
        }

        projectMemberRepository.save(pm);
    }

    public List<ProjectMember> getProjectMembers(Long projectId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            throw new IllegalArgumentException("Projet introuvable");
        }
        return projectMemberRepository.findByProjectId(projectId);
    }

    public Map<String, Boolean> isUserMember(Long projectId, String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Map.of("exists", false, "isMember", false);
        }
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userOpt.get().getId());
        return Map.of("exists", true, "isMember", isMember);
    }
}
