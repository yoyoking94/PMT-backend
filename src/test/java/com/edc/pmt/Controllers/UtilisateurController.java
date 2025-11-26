package com.edc.pmt.Controllers;

import com.edc.pmt.Entities.Utilisateur;
import com.edc.pmt.Services.UtilisateurService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurControllerTest {

    @Mock
    private UtilisateurService utilisateurService;

    @InjectMocks
    private UtilisateurController utilisateurController;

    @Test
    void getUtilisateurById_retourne200EtMasquePassword() {
        Utilisateur u = new Utilisateur();
        u.setId(1L);
        u.setPassword("secret");
        when(utilisateurService.findById(1L)).thenReturn(Optional.of(u));

        ResponseEntity<Utilisateur> response = utilisateurController.getUtilisateurById(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getPassword()).isNull();
    }

    @Test
    void getUtilisateurById_retourne404SiInexistant() {
        when(utilisateurService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Utilisateur> response = utilisateurController.getUtilisateurById(99L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
