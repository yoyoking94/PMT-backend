package com.edc.pmt.Controllers;

import com.edc.pmt.Entities.HistoriqueModification;
import com.edc.pmt.Entities.Tache;
import com.edc.pmt.Services.HistoriqueModificationService;
import com.edc.pmt.Services.TacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TacheControllerTest {

    @Mock
    private TacheService tacheService;

    @Mock
    private HistoriqueModificationService historiqueService;

    @InjectMocks
    private TacheController tacheController;

    @Test
    void getTaches_retourneListe() {
        when(tacheService.getTachesByProjet(1L)).thenReturn(List.of(new Tache()));

        ResponseEntity<List<Tache>> response = tacheController.getTaches(1L);

        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void creerTache_retourne400SiProjetIdManquant() {
        Map<String, Object> body = new HashMap<>();
        ResponseEntity<?> response = tacheController.creerTache(body);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void creerTache_retourne400SiNomManquant() {
        Map<String, Object> body = new HashMap<>();
        body.put("projetId", 1L);
        ResponseEntity<?> response = tacheController.creerTache(body);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void creerTache_retourne400SiPrioriteManquante() {
        Map<String, Object> body = new HashMap<>();
        body.put("projetId", 1L);
        body.put("nom", "Test");
        ResponseEntity<?> response = tacheController.creerTache(body);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void creerTache_retourne200QuandOk() {
        Map<String, Object> body = new HashMap<>();
        body.put("projetId", 1L);
        body.put("nom", "Test");
        body.put("priorite", Tache.Priorite.haute.name());

        Tache saved = new Tache();
        saved.setId(1L);
        when(tacheService.createTacheEtAssigner(any(Tache.class), any()))
                .thenReturn(saved);

        ResponseEntity<?> response = tacheController.creerTache(body);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Tache.class);
    }

    @Test
    void updateTache_retourne200QuandMiseAJour() {
        Tache t = new Tache();
        when(tacheService.updateTache(eq(1L), any(Tache.class), eq(10L), eq("admin")))
                .thenReturn(Optional.of(t));

        ResponseEntity<?> response = tacheController.updateTache(1L, new Tache(), 10L, "admin");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void updateTache_retourne403QuandRefuse() {
        when(tacheService.updateTache(eq(1L), any(Tache.class), eq(10L), eq("membre")))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = tacheController.updateTache(1L, new Tache(), 10L, "membre");

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void deleteTache_retourne404SiInexistante() {
        when(tacheService.getTacheById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = tacheController.deleteTache(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void deleteTache_retourne200QuandSupprime() {
        when(tacheService.getTacheById(1L)).thenReturn(Optional.of(new Tache()));

        ResponseEntity<?> response = tacheController.deleteTache(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(tacheService).deleteTache(1L);
    }

    @Test
    void getTacheById_retourneTacheQuandExiste() {
        Tache t = new Tache();
        t.setId(1L);
        when(tacheService.getTacheById(1L)).thenReturn(Optional.of(t));

        ResponseEntity<Tache> response = tacheController.getTacheById(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getTacheById_retourne404QuandAbsente() {
        when(tacheService.getTacheById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Tache> response = tacheController.getTacheById(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getHistorique_retourneListe() {
        when(historiqueService.getHistoriqueParTache(1L))
                .thenReturn(List.of(new HistoriqueModification()));

        ResponseEntity<List<HistoriqueModification>> response = tacheController.getHistorique(1L);

        assertThat(response.getBody()).hasSize(1);
    }
}
