package com.edc.pmt.Services;

import com.edc.pmt.Entities.HistoriqueModification;
import com.edc.pmt.Repository.HistoriqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsable de la gestion des historiques de modification de tâches.
 * Il permet d'enregistrer les changements effectués sur chaque champ
 * et de récupérer la liste des modifications pour une tâche donnée.
 */
@Service
public class HistoriqueModificationService {

    private final HistoriqueRepository historiqueRepo;

    @Autowired
    public HistoriqueModificationService(HistoriqueRepository historiqueRepo) {
        this.historiqueRepo = historiqueRepo;
    }

    /**
     * Crée et enregistre une nouvelle entrée d’historique.
     *
     * @param tacheId         ID de la tâche modifiée
     * @param utilisateurId   ID de l’utilisateur ayant fait la modification
     * @param champModifie    Nom du champ modifié
     * @param ancienneValeur  Ancienne valeur du champ
     * @param nouvelleValeur  Nouvelle valeur du champ
     */
    @Transactional
    public void enregistrerModification(Long tacheId, Long utilisateurId,
                                        String champModifie, String ancienneValeur, String nouvelleValeur) {
        HistoriqueModification historique = new HistoriqueModification();
        historique.setTacheId(tacheId);
        historique.setUtilisateurId(utilisateurId);
        historique.setChampModifie(champModifie);
        historique.setAncienneValeur(ancienneValeur);
        historique.setNouvelleValeur(nouvelleValeur);

        historiqueRepo.save(historique);
    }

    /**
     * Enregistre automatiquement un changement si l’ancienne et la nouvelle valeur diffèrent.
     *
     * @param tacheId       ID de la tâche concernée
     * @param utilisateurId ID de l’utilisateur effectuant la modification
     * @param champ         Nom du champ modifié
     * @param ancienneValeur Ancienne valeur
     * @param nouvelleValeur Nouvelle valeur
     */
    @Transactional
    public void enregistrerSiModifie(Long tacheId, Long utilisateurId,
                                     String champ, String ancienneValeur, String nouvelleValeur) {
        if (ancienneValeur == null && nouvelleValeur == null) return;
        if (ancienneValeur == null || nouvelleValeur == null || !ancienneValeur.equals(nouvelleValeur)) {
            enregistrerModification(tacheId, utilisateurId, champ, ancienneValeur, nouvelleValeur);
        }
    }

    /**
     * Récupère la liste des modifications associées à une tâche.
     *
     * @param tacheId ID de la tâche
     * @return liste des modifications triées par date décroissante
     */
    @Transactional(readOnly = true)
    public List<HistoriqueModification> getHistoriqueParTache(Long tacheId) {
        return historiqueRepo.findByTacheIdOrderByDateModificationDesc(tacheId);
    }

    /**
     * Supprime tous les historiques liés à une tâche (utile si la tâche est supprimée manuellement).
     * @param tacheId ID de la tâche concernée
     */
    @Transactional
    public void supprimerHistoriqueParTache(Long tacheId) {
        List<HistoriqueModification> logs = historiqueRepo.findByTacheIdOrderByDateModificationDesc(tacheId);
        historiqueRepo.deleteAll(logs);
    }
}
