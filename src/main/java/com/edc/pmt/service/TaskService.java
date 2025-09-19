package com.edc.pmt.service;

import com.edc.pmt.entity.Task;
import com.edc.pmt.entity.Role;

import com.edc.pmt.repository.TaskRepository;
import com.edc.pmt.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<Task> getTasksByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Projet non trouvé");
        }
        return taskRepository.findByProjectId(projectId);
    }

    public Task createTask(Task task) {
        if (task.getProject() == null || !projectRepository.existsById(task.getProject().getId())) {
            throw new IllegalArgumentException("Projet invalide");
        }
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("etudes");
        }
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, Task updatedTask, Long userId) throws IllegalAccessException {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            throw new IllegalArgumentException("Tâche non trouvée");
        }
        Task task = optionalTask.get();

        // Vérification permission (identique à ton code actuel)
        var project = task.getProject();
        boolean isAdmin = project.getProjectMembers().stream()
                .anyMatch(pm -> pm.getUser().getId().equals(userId) && pm.getRole() == Role.ADMIN);
        if (!isAdmin
                && (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(userId))
                && !project.getCreateBy().equals(userId)) {
            throw new IllegalAccessException("Vous n'êtes pas autorisé à modifier cette tâche");
        }

        // Vérification que assignedTo est membre du projet
        if (updatedTask.getAssignedTo() != null) {
            boolean isMember = project.getProjectMembers().stream()
                    .anyMatch(pm -> pm.getUser().getId().equals(updatedTask.getAssignedTo().getId()));
            if (!isMember) {
                throw new IllegalArgumentException("L'utilisateur assigné n'est pas membre du projet");
            }
        }

        // Mise à jour des champs y compris assignedTo
        task.setName(updatedTask.getName());
        task.setDescription(updatedTask.getDescription());
        task.setDueDate(updatedTask.getDueDate());
        task.setPriority(updatedTask.getPriority());
        task.setStatus(updatedTask.getStatus());
        task.setAssignedTo(updatedTask.getAssignedTo());

        return taskRepository.save(task);
    }

}
