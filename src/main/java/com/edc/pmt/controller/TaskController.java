package com.edc.pmt.controller;

import com.edc.pmt.entity.Task;
import com.edc.pmt.repository.TaskRepository;
import com.edc.pmt.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProject(@PathVariable Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(taskRepository.findByProjectId(projectId));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (!projectRepository.existsById(task.getProject().getId())) {
            return ResponseEntity.badRequest().build();
        }
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }
}
