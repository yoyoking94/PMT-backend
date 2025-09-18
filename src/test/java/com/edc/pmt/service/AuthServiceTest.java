package com.edc.pmt.service;

import com.edc.pmt.entity.User;
import com.edc.pmt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signup_whenEmailExists_shouldReturnError() {
        User existingUser = new User();
        existingUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        User newUser = new User();
        newUser.setEmail("test@example.com");

        String result = authService.signup(newUser);
        assertEquals("Erreur : L'email existe déjà.", result);

        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_whenEmailNotExists_shouldSaveUser() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        User newUser = new User();
        newUser.setEmail("new@example.com");

        String result = authService.signup(newUser);
        assertEquals("Inscription réussie", result);

        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void signin_whenUserNotFound_shouldReturnEmpty() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        User loginUser = new User();
        loginUser.setEmail("unknown@example.com");
        loginUser.setPassword("password");

        Optional<User> result = authService.signin(loginUser);
        assertTrue(result.isEmpty());
    }

    @Test
    void signin_whenPasswordIncorrect_shouldReturnEmpty() {
        User existingUser = new User();
        existingUser.setEmail("user@example.com");
        existingUser.setPassword("correctpassword");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        User loginUser = new User();
        loginUser.setEmail("user@example.com");
        loginUser.setPassword("wrongpassword");

        Optional<User> result = authService.signin(loginUser);
        assertTrue(result.isEmpty());
    }

    @Test
    void signin_whenCredentialsCorrect_shouldReturnUser() {
        User existingUser = new User();
        existingUser.setEmail("user@example.com");
        existingUser.setPassword("password");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        User loginUser = new User();
        loginUser.setEmail("user@example.com");
        loginUser.setPassword("password");

        Optional<User> result = authService.signin(loginUser);
        assertTrue(result.isPresent());
        assertEquals("user@example.com", result.get().getEmail());
    }
}
