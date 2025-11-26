package com.edc.pmt.Controllers;

import com.edc.pmt.Entities.MembreProjet;
import com.edc.pmt.Services.MembreProjetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembreProjetControllerTest {

    @Mock
    private MembreProjetService membreProjetService;

    @InjectMocks
    private MembreProjetController membreProjetController;

    @Test
    void getMembresParProjet_retourneListe() {
        when(membreProjetService.getMembresByProjet(1L))
                .thenReturn(List.of(new MembreProjet()));

        ResponseEntity<List<MembreProjet>> response = membreProjetController.getMembresParProjet(1L);

        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void addMembreByEmail_retourne200QuandOk() {
        MembreProjet m = new MembreProjet(1L, 1L, 2L, "membre");
        when(membreProjetService.addMembreByEmail(1L, "a@b.c", "membre", 10L)).thenReturn(m);

        ResponseEntity<?> response = membreProjetController.addMembreByEmail(1L, "a@b.c", "membre", 10L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(MembreProjet.class);
    }

    @Test
    void addMembreByEmail_retourne400QuandException() {
        when(membreProjetService.addMembreByEmail(1L, "a@b.c", "membre", 10L))
                .thenThrow(new RuntimeException("erreur"));

        ResponseEntity<?> response = membreProjetController.addMembreByEmail(1L, "a@b.c", "membre", 10L);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void removeMembre_appelleService() {
        ResponseEntity<?> response = membreProjetController.removeMembre(5L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(membreProjetService).removeMembre(5L);
    }
}
