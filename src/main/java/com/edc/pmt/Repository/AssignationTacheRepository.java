package com.edc.pmt.Repository;

import com.edc.pmt.Entities.AssignationTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignationTacheRepository extends JpaRepository<AssignationTache, Long> {
    List<AssignationTache> findByTacheId(Long tacheId);
}
