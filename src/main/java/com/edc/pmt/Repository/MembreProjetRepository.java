package com.edc.pmt.Repository;

import com.edc.pmt.Entities.MembreProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembreProjetRepository extends JpaRepository<MembreProjet, Long> {
    List<MembreProjet> findByProjetId(Long projetId);

    boolean existsByProjetIdAndUtilisateurId(Long projetId, Long utilisateurId);
    
};