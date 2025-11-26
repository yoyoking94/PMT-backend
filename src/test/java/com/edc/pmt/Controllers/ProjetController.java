package com.edc.pmt.Controllers;

import com.edc.pmt.Entities.Projet;
import com.edc.pmt.Services.ProjetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetControllerTest {

    @Mock
    private ProjetService projetService;

    @InjectMocks
    private ProjetController projetController;

    @Test
    void createProject_retourne200QuandOk() {
        Projet p = new Projet();
        when(projetService.creerProjet(any(Projet.class))).thenReturn(p);

        ResponseEntity<?> response = projetController.createProject(new Projet());

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Projet.class);
    }

    @Test
    void createProject_retourne500EnCasException() {
        when(projetService.creerProjet(any(Projet.class))).thenThrow(new RuntimeException("err"));

        ResponseEntity<?> response = projetController.createProject(new Projet());

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void getMyProjects_retourneListe() {
        when(projetService.getProjetsByCreateur(10L)).thenReturn(List.of(new Projet()));

        ResponseEntity<List<Projet>> response = projetController.getMyProjects(10L);

        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getAllProjects_retourneListe() {
        when(projetService.getTousProjets()).thenReturn(List.of(new Projet(), new Projet()));

        ResponseEntity<List<Projet>> response = projetController.getAllProjects();

        assertThat(response.getBody()).hasSize(2);
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteProject_retourne200QuandAutorise() {
        when(projetService.deleteProjetIfAuthorized(1L, 10L)).thenReturn(true);

        ResponseEntity<?> response = projetController.deleteProject(1L, 10L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat((Map<String, Object>) response.getBody()).containsKey("message");
    }

    @Test
    void deleteProject_retourne403QuandRefuse() {
        when(projetService.deleteProjetIfAuthorized(1L, 10L)).thenReturn(false);

        ResponseEntity<?> response = projetController.deleteProject(1L, 10L);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void getProjectById_retourneProjetQuandExiste() {
        Projet p = new Projet();
        p.setId(1L);
        when(projetService.getProjetById(1L)).thenReturn(Optional.of(p));

        ResponseEntity<?> response = projetController.getProjectById(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Projet.class);
    }

    @Test
    void getProjectById_retourne404QuandAbsent() {
        when(projetService.getProjetById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = projetController.getProjectById(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void updateProject_retourne200QuandAutorise() {
        Projet updated = new Projet();
        updated.setId(1L);
        when(projetService.updateProjet(eq(1L), any(Projet.class), eq(10L)))
                .thenReturn(Optional.of(updated));

        ResponseEntity<?> response = projetController.updateProject(1L, new Projet(), 10L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Projet.class);
    }

    @Test
    void updateProject_retourne403QuandRefuse() {
        when(projetService.updateProjet(eq(1L), any(Projet.class), eq(10L)))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = projetController.updateProject(1L, new Projet(), 10L);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }
}
