package com.edc.pmt.Services;

import com.edc.pmt.Entities.Utilisateur;
import com.edc.pmt.Repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Optional<Utilisateur> findById(Long id) {
        return utilisateurRepository.findById(id);
    }

    public Optional<Utilisateur> findByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    public Long getUserIdByEmail(String email) {
        return utilisateurRepository.findByEmail(email).map(Utilisateur::getId).orElse(null);
    }

    public Utilisateur signup(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    public Optional<Utilisateur> signin(String email, String password) {
        return utilisateurRepository.findByEmail(email).filter(u -> u.getPassword().equals(password));
    }
};