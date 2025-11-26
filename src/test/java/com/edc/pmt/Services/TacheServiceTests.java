package com.edc.pmt.Services;

import com.edc.pmt.Entities.*;
import com.edc.pmt.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TacheServiceTest {

    @Mock
    private TacheRepository tacheRepo;
    @Mock
    private AssignationTacheRepository assignationRepo;
    @Mock
    private UtilisateurRepository utilisateurRepo;
    @Mock
    private EmailService emailService;
    @Mock
    private HistoriqueModificationService historiqueService;

    @InjectMocks
    private TacheService tacheService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTachesByProjet() {
        Tache t1 = new Tache();
        t1.setId(1L);
        Tache t2 = new Tache();
        t2.setId(2L);

        AssignationTache assign1 = new AssignationTache();
        assign1.setTacheId(1L);
        assign1.setUtilisateurId(11L);

        when(tacheRepo.findByProjetId(100L)).thenReturn(List.of(t1, t2));
        when(assignationRepo.findByTacheId(1L)).thenReturn(List.of(assign1));
        when(assignationRepo.findByTacheId(2L)).thenReturn(Collections.emptyList());

        List<Tache> taches = tacheService.getTachesByProjet(100L);

        assertEquals(2, taches.size());
        assertEquals(11L, taches.get(0).getMembreId());
        assertNull(taches.get(1).getMembreId());
        verify(tacheRepo).findByProjetId(100L);
        verify(assignationRepo, times(2)).findByTacheId(anyLong());
    }

    @Test
    void testGetTacheById_AssignationsVides() {
        Tache tache = new Tache();
        tache.setId(123L);

        when(tacheRepo.findById(123L)).thenReturn(Optional.of(tache));
        when(assignationRepo.findByTacheId(123L)).thenReturn(Collections.emptyList());

        Optional<Tache> result = tacheService.getTacheById(123L);

        assertTrue(result.isPresent());
        assertNull(result.get().getMembreId());
    }

    @Test
    void testCreateTacheEtAssigner() {
        Tache newTache = new Tache();
        newTache.setNom("Test");
        newTache.setCreateurId(5L);

        Tache savedTache = new Tache();
        savedTache.setId(9L);
        savedTache.setNom("Test");
        savedTache.setCreateurId(5L);

        Utilisateur user = new Utilisateur();
        user.setId(5L);
        user.setEmail("user@test.com");

        when(tacheRepo.save(newTache)).thenReturn(savedTache);
        when(utilisateurRepo.findById(5L)).thenReturn(Optional.of(user));
        when(assignationRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Tache result = tacheService.createTacheEtAssigner(newTache, 5L);

        assertNotNull(result);
        assertEquals(9L, result.getId());
        assertEquals(5L, result.getMembreId());
        verify(emailService).sendSimpleMessage(eq("user@test.com"), anyString(), anyString());
    }

    @Test
    void testUpdateTache_AdminChanges() {
        Long tacheId = 1L, userId = 10L;

        Tache original = new Tache();
        original.setId(tacheId);
        original.setNom("Old Name");
        original.setDescription("Old Desc");
        original.setPriorite(Tache.Priorite.moyenne);
        original.setStatut(Tache.Statut.a_faire);

        Tache updated = new Tache();
        updated.setNom("New Name");
        updated.setDescription("New Desc");
        updated.setPriorite(Tache.Priorite.haute);
        updated.setStatut(Tache.Statut.en_cours);

        when(tacheRepo.findById(tacheId)).thenReturn(Optional.of(original));
        doNothing().when(historiqueService).enregistrerSiModifie(anyLong(), anyLong(), anyString(), any(), any());
        when(assignationRepo.findByTacheId(tacheId)).thenReturn(Collections.emptyList());
        when(utilisateurRepo.findById(anyLong())).thenReturn(Optional.empty());
        when(tacheRepo.save(any())).thenReturn(original);

        Optional<Tache> result = tacheService.updateTache(tacheId, updated, userId, "administrateur");

        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getNom());
        assertEquals(Tache.Statut.en_cours, result.get().getStatut());

        verify(historiqueService, atLeastOnce()).enregistrerSiModifie(anyLong(), anyLong(), anyString(), any(), any());
        verify(tacheRepo).save(original);
    }

    @Test
    void testUpdateTache_MembreChangeStatutSeulement() {
        Long tacheId = 2L, userId = 22L;
        Tache original = new Tache();
        original.setId(tacheId);
        original.setStatut(Tache.Statut.a_faire);
        original.setPriorite(Tache.Priorite.moyenne); // AJOUT

        Tache updated = new Tache();
        updated.setStatut(Tache.Statut.en_cours);
        updated.setPriorite(Tache.Priorite.moyenne); // AJOUT pour cohérence

        when(tacheRepo.findById(tacheId)).thenReturn(Optional.of(original));
        when(tacheRepo.save(any())).thenReturn(original);

        Optional<Tache> result = tacheService.updateTache(tacheId, updated, userId, "membre");

        assertTrue(result.isPresent());
        assertEquals(Tache.Statut.en_cours, result.get().getStatut());
    }

    @Test
    void testUpdateTache_ObservateurAccesRefuse() {
        Long tacheId = 3L, userId = 33L;
        Tache original = new Tache();
        original.setId(tacheId);

        Tache updated = new Tache();
        updated.setStatut(Tache.Statut.terminee);

        when(tacheRepo.findById(tacheId)).thenReturn(Optional.of(original));
        Optional<Tache> result = tacheService.updateTache(tacheId, updated, userId, "observateur");
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateTache_AdminEffaceAssignationExistante() {
        Long tacheId = 4L, userId = 44L;

        Tache original = new Tache();
        original.setId(tacheId);
        original.setNom("Tâche A");
        original.setStatut(Tache.Statut.a_faire);
        original.setPriorite(Tache.Priorite.moyenne);

        Tache updated = new Tache();
        updated.setNom("Tâche A");
        updated.setPriorite(Tache.Priorite.moyenne);
        updated.setStatut(Tache.Statut.a_faire);

        AssignationTache assign = new AssignationTache();
        assign.setTacheId(tacheId);
        assign.setUtilisateurId(99L);

        when(tacheRepo.findById(tacheId)).thenReturn(Optional.of(original));
        when(assignationRepo.findByTacheId(tacheId)).thenReturn(List.of(assign));
        when(tacheRepo.save(any())).thenReturn(original);

        Optional<Tache> result = tacheService.updateTache(tacheId, updated, userId, "administrateur");

        assertTrue(result.isPresent());
    }

    @Test
    void testUpdateTache_AdminNouvelleAssignation() {
        Long tacheId = 5L, userId = 55L;

        Tache original = new Tache();
        original.setId(tacheId);
        original.setPriorite(Tache.Priorite.moyenne);
        original.setStatut(Tache.Statut.a_faire);

        Tache updated = new Tache();
        updated.setMembreId(100L);
        updated.setPriorite(Tache.Priorite.moyenne);
        updated.setStatut(Tache.Statut.a_faire);

        when(tacheRepo.findById(tacheId)).thenReturn(Optional.of(original));
        when(assignationRepo.findByTacheId(tacheId)).thenReturn(Collections.emptyList());
        when(utilisateurRepo.findById(100L)).thenReturn(Optional.empty());
        when(assignationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tacheRepo.save(any())).thenReturn(original);

        Optional<Tache> result = tacheService.updateTache(tacheId, updated, userId, "administrateur");

        assertTrue(result.isPresent());
    }

    @Test
    void testDeleteTache() {
        doNothing().when(tacheRepo).deleteById(3L);
        tacheService.deleteTache(3L);
        verify(tacheRepo).deleteById(3L);
    }
}
