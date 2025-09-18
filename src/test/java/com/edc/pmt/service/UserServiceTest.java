package com.edc.pmt.service;

import com.edc.pmt.entity.User;
import com.edc.pmt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsers_shouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(new User()));

        List<User> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findByEmail_shouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void emailExists_shouldReturnTrueIfExists() {
        when(userRepository.findByEmail("exists@example.com")).thenReturn(Optional.of(new User()));

        assertTrue(userService.emailExists("exists@example.com"));
    }

    @Test
    void emailExists_shouldReturnFalseIfNotExists() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertFalse(userService.emailExists("notfound@example.com"));
    }
}
