package com.edc.pmt.service;

import com.edc.pmt.entity.User;
import com.edc.pmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public String signup(User newUser) {
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return "Erreur : L'email existe déjà.";
        }
        userRepository.save(newUser);
        return "Inscription réussie";
    }

    public Optional<User> signin(User loginUser) {
        Optional<User> userOpt = userRepository.findByEmail(loginUser.getEmail());
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(loginUser.getPassword())) {
            return Optional.empty();
        }
        return userOpt;
    }
}
