package com.edc.pmt.service;

import com.edc.pmt.entity.Project;
import com.edc.pmt.entity.ProjectMember;
import com.edc.pmt.entity.User;
import com.edc.pmt.repository.ProjectMemberRepository;
import com.edc.pmt.repository.ProjectRepository;
import com.edc.pmt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllProjects_shouldReturnProjects() {
        Project project = new Project();
        project.setName("Projet Test");
        when(projectRepository.findAll()).thenReturn(List.of(project));

        List<Project> projects = projectService.getAllProjects();

        assertFalse(projects.isEmpty());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void createProject_shouldSaveProjectAndAddAdmin() {
        Project project = new Project();
        project.setCreateBy(1L);
        project.setName("Nouveau projet");
        when(projectRepository.save(project)).thenReturn(project);

        User creator = new User();
        creator.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));

        Project savedProject = projectService.createProject(project);
        assertEquals("Nouveau projet", savedProject.getName());

        verify(projectRepository, times(1)).save(project);
        verify(projectMemberRepository, times(1)).save(any(ProjectMember.class));
    }

    @Test
    void deleteProject_whenNotCreator_shouldThrow() {
        Project project = new Project();
        project.setCreateBy(1L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        IllegalAccessException thrown = assertThrows(IllegalAccessException.class, () -> {
            projectService.deleteProject(10L, 2L);
        });

        assertEquals("Non autorisé à supprimer ce projet", thrown.getMessage());
    }

    @Test
    void deleteProject_whenCreator_shouldDelete() throws Exception {
        Project project = new Project();
        project.setCreateBy(1L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        projectService.deleteProject(10L, 1L);

        verify(projectRepository, times(1)).delete(project);
    }

    // Ajouter d'autres tests pour updateProject, inviteMember, removeMember...
}
