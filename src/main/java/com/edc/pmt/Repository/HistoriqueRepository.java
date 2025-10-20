package com.edc.pmt.Repository;

import com.edc.pmt.Entities.HistoriqueModification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<HistoriqueModification, Long> {
    List<HistoriqueModification> findByTacheIdOrderByDateModificationDesc(Long tacheId);
}
