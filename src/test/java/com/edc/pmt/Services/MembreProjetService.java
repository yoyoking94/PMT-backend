package com.edc.pmt.Services;

import com.edc.pmt.Entities.MembreProjet;
import com.edc.pmt.Repository.MembreProjetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembreProjetServiceTest {

    @Mock
    private MembreProjetRepository membreProjetRepository;

    @Mock
    private UtilisateurService utilisateurService;

    @InjectMocks
    private MembreProjetService membreProjetService;

    @Test
    void getMembresByProjet_retourneListe() {
        when(membreProjetRepository.findByProjetId(1L))
                .thenReturn(List.of(new MembreProjet()));

        List<MembreProjet> result = membreProjetService.getMembresByProjet(1L);

        assertThat(result).hasSize(1);
        verify(membreProjetRepository).findByProjetId(1L);
    }

    @Test
    void isAdmin_retourneTrueQuandRoleAdmin() {
        MembreProjet m = new MembreProjet(1L, 1L, 10L, "administrateur");
        when(membreProjetRepository.findByProjetId(1L)).thenReturn(List.of(m));

        boolean admin = membreProjetService.isAdmin(1L, 10L);

        assertThat(admin).isTrue();
    }

    @Test
    void isAdmin_retourneFalseSiPasAdmin() {
        MembreProjet m = new MembreProjet(1L, 1L, 10L, "membre");
        when(membreProjetRepository.findByProjetId(1L)).thenReturn(List.of(m));

        boolean admin = membreProjetService.isAdmin(1L, 10L);

        assertThat(admin).isFalse();
    }

    @Test
    void addMembreByEmail_lanceExceptionSiDemandeurNonAdmin() {
        when(membreProjetRepository.findByProjetId(1L)).thenReturn(List.of());

        assertThatThrownBy(() ->
                membreProjetService.addMembreByEmail(1L, "a@b.c", "membre", 10L)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Seuls les administrateurs");

        verify(membreProjetRepository, never()).save(any());
    }

    @Test
    void addMembreByEmail_lanceExceptionSiUserIntrouvable() {
        // demandeur est admin
        MembreProjet admin = new MembreProjet(1L, 1L, 10L, "administrateur");
        when(membreProjetRepository.findByProjetId(1L)).thenReturn(List.of(admin));
        when(utilisateurService.getUserIdByEmail("x@y.z")).thenReturn(null);

        assertThatThrownBy(() ->
                membreProjetService.addMembreByEmail(1L, "x@y.z", "membre", 10L)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    void addMembreByEmail_lanceExceptionSiDejaMembre() {
        MembreProjet admin = new MembreProjet(1L, 1L, 10L, "administrateur");
        when(membreProjetRepository.findByProjetId(1L)).thenReturn(List.of(admin));
        when(utilisateurService.getUserIdByEmail("a@b.c")).thenReturn(20L);
        when(membreProjetRepository.existsByProjetIdAndUtilisateurId(1L, 20L))
                .thenReturn(true);

        assertThatThrownBy(() ->
                membreProjetService.addMembreByEmail(1L, "a@b.c", "membre", 10L)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Membre déjà présent");
    }

    @Test
    void addMembreByEmail_ajouteMembreQuandToutOk() {
        MembreProjet admin = new MembreProjet(1L, 1L, 10L, "administrateur");
        when(membreProjetRepository.findByProjetId(1L)).thenReturn(List.of(admin));
        when(utilisateurService.getUserIdByEmail("a@b.c")).thenReturn(20L);
        when(membreProjetRepository.existsByProjetIdAndUtilisateurId(1L, 20L)).thenReturn(false);
        when(membreProjetRepository.save(any(MembreProjet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MembreProjet result = membreProjetService.addMembreByEmail(1L, "a@b.c", "membre", 10L);

        assertThat(result.getProjetId()).isEqualTo(1L);
        assertThat(result.getUtilisateurId()).isEqualTo(20L);
        assertThat(result.getRole()).isEqualTo("membre");
        verify(membreProjetRepository).save(any(MembreProjet.class));
    }

    @Test
    void removeMembre_supprime() {
        membreProjetService.removeMembre(5L);

        verify(membreProjetRepository).deleteById(5L);
    }
}
