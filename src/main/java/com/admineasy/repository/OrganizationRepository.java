// src/main/java/com/admineasy/repository/OrganizationRepository.java
package com.admineasy.repository;

import com.admineasy.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);
    // Eventuellement, on peut ajouter findByEmailOfAdmin, etc.
}
