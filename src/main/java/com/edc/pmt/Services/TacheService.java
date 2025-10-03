package com.edc.pmt.Services;

import com.edc.pmt.Entities.Tache;
import com.edc.pmt.Entities.AssignationTache;
import com.edc.pmt.Repository.TacheRepository;
import com.edc.pmt.Repository.AssignationTacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TacheService {
    private final TacheRepository tacheRepo;
    private final AssignationTacheRepository assignRepo;

    public TacheService(TacheRepository tacheRepo, AssignationTacheRepository assignRepo) {
        this.tacheRepo = tacheRepo;
        this.assignRepo = assignRepo;
    }

    public List<Tache> getTachesByProjet(Long projetId) {
        List<Tache> taches = tacheRepo.findByProjetId(projetId);
        for (Tache t : taches) {
            List<AssignationTache> asigns = assignRepo.findByTacheId(t.getId());
            if (!asigns.isEmpty()) {
                t.setMembreId(asigns.get(0).getUtilisateurId());
            }
        }
        return taches;
    }

    @Transactional
    public Tache createTacheEtAssigner(Tache tache, Long membreId) {
        Tache saved = tacheRepo.save(tache);
        if (membreId != null) {
            AssignationTache assign = new AssignationTache();
            assign.setTacheId(saved.getId());
            assign.setUtilisateurId(membreId);
            assignRepo.save(assign);
        }
        return saved;
    }
}
