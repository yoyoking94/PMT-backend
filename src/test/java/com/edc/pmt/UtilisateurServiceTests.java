package com.edc.pmt;

import com.edc.pmt.Entities.Utilisateur;
import com.edc.pmt.Repository.UtilisateurRepository;
import com.edc.pmt.Services.UtilisateurService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private UtilisateurService utilisateurService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Found() {
        Utilisateur user = new Utilisateur();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<Utilisateur> result = utilisateurService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testFindById_NotFound() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Utilisateur> result = utilisateurService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmail_Found() {
        Utilisateur user = new Utilisateur();
        user.setId(2L);
        user.setEmail("john@example.com");

        when(utilisateurRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<Utilisateur> result = utilisateurService.findByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId());
    }

    @Test
    void testFindByEmail_NotFound() {
        when(utilisateurRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<Utilisateur> result = utilisateurService.findByEmail("unknown@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetUserIdByEmail_Found() {
        Utilisateur user = new Utilisateur();
        user.setId(10L);
        user.setEmail("abc@example.com");

        when(utilisateurRepository.findByEmail("abc@example.com")).thenReturn(Optional.of(user));

        Long userId = utilisateurService.getUserIdByEmail("abc@example.com");

        assertEquals(10L, userId);
    }

    @Test
    void testGetUserIdByEmail_NotFound() {
        when(utilisateurRepository.findByEmail("abc@example.com")).thenReturn(Optional.empty());

        Long userId = utilisateurService.getUserIdByEmail("abc@example.com");

        assertNull(userId);
    }

    @Test
    void testSignup() {
        Utilisateur userToSave = new Utilisateur();
        userToSave.setEmail("newuser@example.com");

        Utilisateur savedUser = new Utilisateur();
        savedUser.setId(20L);
        savedUser.setEmail("newuser@example.com");

        when(utilisateurRepository.save(userToSave)).thenReturn(savedUser);

        Utilisateur result = utilisateurService.signup(userToSave);

        assertNotNull(result);
        assertEquals(20L, result.getId());
    }

    @Test
    void testSignin_Success() {
        Utilisateur user = new Utilisateur();
        user.setEmail("login@example.com");
        user.setPassword("password123");

        when(utilisateurRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));

        Optional<Utilisateur> result = utilisateurService.signin("login@example.com", "password123");

        assertTrue(result.isPresent());
    }

    @Test
    void testSignin_FailWrongPassword() {
        Utilisateur user = new Utilisateur();
        user.setEmail("login@example.com");
        user.setPassword("password123");

        when(utilisateurRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));

        Optional<Utilisateur> result = utilisateurService.signin("login@example.com", "wrongpassword");

        assertFalse(result.isPresent());
    }

    @Test
    void testSignin_NoUser() {
        when(utilisateurRepository.findByEmail("nouser@example.com")).thenReturn(Optional.empty());

        Optional<Utilisateur> result = utilisateurService.signin("nouser@example.com", "anyPassword");

        assertFalse(result.isPresent());
    }
}
