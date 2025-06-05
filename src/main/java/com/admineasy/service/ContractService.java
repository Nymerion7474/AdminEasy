// src/main/java/com/admineasy/service/ContractService.java
package com.admineasy.service;

import com.admineasy.model.Contract;
import com.admineasy.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepo;

    /**
     * Sauvegarde ou met à jour un contrat.
     */
    public Contract save(Contract contract) {
        return contractRepo.save(contract);
    }

    /**
     * Recherche un contrat par id (sans filtre org).
     */
    public Optional<Contract> findById(Long id) {
        return contractRepo.findById(id);
    }

    /**
     * Liste tous les contrats d’une organisation.
     */
    public List<Contract> findByOrganizationId(Long orgId) {
        return contractRepo.findByOrganization_Id(orgId);
    }

    /**
     * Recherche un contrat par id + orgId.
     */
    public Optional<Contract> findByIdAndOrganizationId(Long id, Long orgId) {
        return contractRepo.findByIdAndOrganization_Id(id, orgId);
    }

    /**
     * Supprime un contrat si l’orgId correspond.
     *
     * @return true si supprimé, false si pas trouvé ou pas dans la bonne org.
     */
    public boolean deleteByIdAndOrganizationId(Long id, Long orgId) {
        Optional<Contract> opt = contractRepo.findByIdAndOrganization_Id(id, orgId);
        if (opt.isPresent()) {
            contractRepo.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Récupère tous les contrats d’une org dont la date de fin est exactement la date passée.
     * Utile pour envoyer les rappels « X jours avant »
     */
    public List<Contract> findByOrganizationIdAndEndDate(Long orgId, LocalDate date) {
        return contractRepo.findByOrganization_IdAndEndDate(orgId, date);
    }
}
