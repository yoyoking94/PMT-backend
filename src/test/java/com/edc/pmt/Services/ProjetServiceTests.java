package com.edc.pmt.Services;

import com.edc.pmt.Entities.MembreProjet;
import com.edc.pmt.Entities.Projet;
import com.edc.pmt.Repository.MembreProjetRepository;
import com.edc.pmt.Repository.ProjetRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class ProjetServiceTests {

    @Mock
    private ProjetRepository projetRepository;

    @Mock
    private MembreProjetRepository membreProjetRepository;

    @InjectMocks
    private ProjetService projetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreerProjet() {
        Projet projet = new Projet();
        projet.setNom("Projet Test");
        projet.setCreateurId(1L);

        Projet savedProjet = new Projet();
        savedProjet.setId(10L);
        savedProjet.setNom(projet.getNom());
        savedProjet.setCreateurId(projet.getCreateurId());

        when(projetRepository.save(projet)).thenReturn(savedProjet);
        when(membreProjetRepository.save(any(MembreProjet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Projet result = projetService.creerProjet(projet);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(projetRepository, times(1)).save(projet);
        verify(membreProjetRepository, times(1)).save(any(MembreProjet.class));
    }

    @Test
    void testCanEditProject_Owner() {
        Projet projet = new Projet();
        projet.setId(1L);
        projet.setCreateurId(5L);

        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));

        boolean canEdit = projetService.canEditProject(1L, 5L);

        assertTrue(canEdit);
    }

    @Test
    void testCanEditProject_Member() {
        Projet projet = new Projet();
        projet.setId(1L);
        projet.setCreateurId(5L);

        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(membreProjetRepository.existsByProjetIdAndUtilisateurId(1L, 7L)).thenReturn(true);

        boolean canEdit = projetService.canEditProject(1L, 7L);

        assertTrue(canEdit);
    }

    @Test
    void testCanEditProject_None() {
        when(projetRepository.findById(1L)).thenReturn(Optional.empty());

        boolean canEdit = projetService.canEditProject(1L, 7L);

        assertFalse(canEdit);
    }

    @Test
    void testUpdateProjet_Success() {
        Projet existingProjet = new Projet();
        existingProjet.setId(1L);
        existingProjet.setNom("Original");
        existingProjet.setDescription("Desc");

        // Conversion java.util.Date -> java.sql.Date
        Date utilDate = new Date();
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        existingProjet.setDateDebut(sqlDate);

        Projet updatedProjet = new Projet();
        updatedProjet.setNom("Modifié");
        updatedProjet.setDescription("Desc modifiée");
        updatedProjet.setDateDebut(sqlDate);

        when(projetRepository.findById(1L)).thenReturn(Optional.of(existingProjet));
        when(membreProjetRepository.existsByProjetIdAndUtilisateurId(1L, 10L)).thenReturn(true);
        when(projetRepository.save(existingProjet)).thenReturn(existingProjet);

        Optional<Projet> result = projetService.updateProjet(1L, updatedProjet, 10L);

        assertTrue(result.isPresent());
        assertEquals("Modifié", result.get().getNom());
        assertEquals("Desc modifiée", result.get().getDescription());
    }

    @Test
    void testUpdateProjet_FailUnauthorized() {
        when(projetRepository.findById(1L)).thenReturn(Optional.of(new Projet()));
        when(membreProjetRepository.existsByProjetIdAndUtilisateurId(1L, 10L)).thenReturn(false);

        Optional<Projet> result = projetService.updateProjet(1L, new Projet(), 10L);

        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteProjetIfAuthorized_Success() {
        Projet projet = new Projet();
        projet.setId(1L);
        projet.setCreateurId(5L);

        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(membreProjetRepository.existsByProjetIdAndUtilisateurId(1L, 5L)).thenReturn(true);
        doNothing().when(projetRepository).deleteById(1L);

        boolean result = projetService.deleteProjetIfAuthorized(1L, 5L);

        assertTrue(result);
        verify(projetRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProjetIfAuthorized_Fail() {
        when(projetRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = projetService.deleteProjetIfAuthorized(1L, 10L);

        assertFalse(result);
        verify(projetRepository, never()).deleteById(anyLong());
    }
}
