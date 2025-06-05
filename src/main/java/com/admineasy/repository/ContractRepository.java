// src/main/java/com/admineasy/repository/ContractRepository.java
package com.admineasy.repository;

import com.admineasy.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    // Récupérer tous les contrats d’une organisation
    List<Contract> findByOrganization_Id(Long orgId);

    // Récupérer un contrat par son id ET son organization_id
    Optional<Contract> findByIdAndOrganization_Id(Long id, Long orgId);

    // Méthode pour scheduler : tous les contrats d’une org dont la date de fin est exactement x jours plus tard
    List<Contract> findByOrganization_IdAndEndDate(Long orgId, LocalDate date);
}
