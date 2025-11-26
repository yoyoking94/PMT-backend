package com.edc.pmt.Services;

import com.edc.pmt.Entities.HistoriqueModification;
import com.edc.pmt.Repository.HistoriqueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoriqueModificationServiceTest {

    @Mock
    private HistoriqueRepository historiqueRepository;

    @InjectMocks
    private HistoriqueModificationService historiqueService;

    @Test
    void enregistrerModification_creeEtSauvegarde() {
        historiqueService.enregistrerModification(1L, 2L, "champ", "old", "new");

        verify(historiqueRepository).save(any(HistoriqueModification.class));
    }

    @Test
    void enregistrerSiModifie_neRienFaireSiValeursNulles() {
        historiqueService.enregistrerSiModifie(1L, 2L, "champ", null, null);

        verify(historiqueRepository, never()).save(any());
    }

    @Test
    void enregistrerSiModifie_creeSiValeursDifferentes() {
        historiqueService.enregistrerSiModifie(1L, 2L, "champ", "old", "new");

        verify(historiqueRepository).save(any(HistoriqueModification.class));
    }

    @Test
    void getHistoriqueParTache_retourneListe() {
        when(historiqueRepository.findByTacheIdOrderByDateModificationDesc(1L))
                .thenReturn(List.of(new HistoriqueModification()));

        List<HistoriqueModification> result = historiqueService.getHistoriqueParTache(1L);

        assertThat(result).hasSize(1);
        verify(historiqueRepository).findByTacheIdOrderByDateModificationDesc(1L);
    }

    @Test
    void supprimerHistoriqueParTache_supprimeTous() {
        List<HistoriqueModification> logs = List.of(new HistoriqueModification(), new HistoriqueModification());
        when(historiqueRepository.findByTacheIdOrderByDateModificationDesc(1L))
                .thenReturn(logs);

        historiqueService.supprimerHistoriqueParTache(1L);

        verify(historiqueRepository).deleteAll(logs);
    }
}
