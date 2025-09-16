package com.edc.pmt.controller;

import com.edc.pmt.entity.Task;
import com.edc.pmt.entity.Project;
import com.edc.pmt.repository.TaskRepository;
import com.edc.pmt.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;

    // Retourne les tâches du projet
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProject(@PathVariable Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(taskRepository.findByProjectId(projectId));
    }

    // Création d'une tâche
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task.getProject() == null || !projectRepository.existsById(task.getProject().getId())) {
            return ResponseEntity.badRequest().build();
        }
        // Ici, on peut ajouter une valeur par défaut pour status si besoin
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("etudes");
        }
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    // Modification d'une tâche par admin, assigné ou créateur
    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long taskId,
            @RequestBody Task updatedTask,
            @RequestParam Long userId) {
        var optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Task task = optionalTask.get();
        Project project = task.getProject();

        boolean isAdmin = project.getProjectMembers().stream()
                .anyMatch(pm -> pm.getUser().getId().equals(userId) && pm.getRole().equals("ADMIN"));

        if (!isAdmin
                && (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(userId))
                && !project.getCreateBy().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'êtes pas autorisé à modifier cette tâche");
        }

        task.setName(updatedTask.getName());
        task.setDescription(updatedTask.getDescription());
        task.setDueDate(updatedTask.getDueDate()); // remplace dueDate
        task.setPriority(updatedTask.getPriority());
        task.setStatus(updatedTask.getStatus()); // nouveau champ statut
        taskRepository.save(task);
        return ResponseEntity.ok(task);
    }
}
