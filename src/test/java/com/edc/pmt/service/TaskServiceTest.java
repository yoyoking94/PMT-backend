package com.edc.pmt.service;

import com.edc.pmt.entity.Task;
import com.edc.pmt.entity.Project;
import com.edc.pmt.entity.ProjectMember;
import com.edc.pmt.entity.Role;
import com.edc.pmt.entity.User;
import com.edc.pmt.repository.ProjectRepository;
import com.edc.pmt.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTask_shouldSaveTask() {
        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setProject(project);
        task.setStatus(null);

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);

        Task saved = taskService.createTask(task);
        assertEquals("etudes", saved.getStatus());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void getTasksByProject_whenProjectExists_shouldReturnTasks() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of(new Task()));

        List<Task> tasks = taskService.getTasksByProject(1L);
        assertFalse(tasks.isEmpty());
    }

    @Test
    void updateTask_authorizedUser_shouldUpdateTask() throws Exception {
        User user = new User();
        user.setId(1L);

        ProjectMember adminMember = new ProjectMember();
        adminMember.setRole(Role.ADMIN); // Enum role
        adminMember.setUser(user);

        Project project = new Project();
        project.setCreateBy(2L);

        // Correction : Set.of(adminMember) directement pour projet membres
        project.setProjectMembers(Set.of(adminMember));

        Task task = new Task();
        task.setProject(project);
        task.setAssignedTo(user);

        when(taskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task updated = new Task();
        updated.setName("Modifiée");
        updated.setDescription("Desc");
        updated.setDueDate(LocalDate.now());
        updated.setPriority("Haute");
        updated.setStatus("En cours");

        Task result = taskService.updateTask(1L, updated, 1L);
        assertEquals("Modifiée", result.getName());
    }

    @Test
    void updateTask_userNotAuthorized_shouldThrowException() {
        User user = new User();
        user.setId(1L);

        ProjectMember member = new ProjectMember();
        member.setRole(Role.MEMBER); // Utilisateur avec rôle MEMBER (pas admin)
        member.setUser(user);

        Project project = new Project();
        project.setCreateBy(2L); // Créateur différent

        project.setProjectMembers(Set.of(member));

        Task task = new Task();
        task.setProject(project);

        task.setAssignedTo(null);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task updatedTask = new Task();
        updatedTask.setName("Updated");

        Exception exception = assertThrows(IllegalAccessException.class, () -> {
            taskService.updateTask(1L, updatedTask, 1L);
        });

        assertEquals("Vous n'êtes pas autorisé à modifier cette tâche", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_taskNotFound_shouldThrowException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        Task updatedTask = new Task();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(1L, updatedTask, 1L);
        });

        assertEquals("Tâche non trouvée", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }
}
