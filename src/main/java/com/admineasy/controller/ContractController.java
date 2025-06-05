// src/main/java/com/admineasy/controller/ContractController.java
package com.admineasy.controller;

import com.admineasy.dto.ContractDto;
import com.admineasy.model.Contract;
import com.admineasy.model.User;
import com.admineasy.service.ContractService;
import com.admineasy.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Validated
public class ContractController {

    private final ContractService contractService;
    private final OcrService ocrService;

    /**
     * GET /api/contracts → liste de DTO
     */
    @GetMapping
    public List<ContractDto> getAll() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long orgId = currentUser.getOrganization().getId();
        return contractService.findByOrganizationId(orgId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * GET /api/contracts/{id} → un seul DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContractDto> getById(@PathVariable Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long orgId = currentUser.getOrganization().getId();
        return contractService.findByIdAndOrganizationId(id, orgId)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/contracts
     * Crée un contrat en vérifiant startDate <= endDate.
     */
    @PostMapping
    public ContractDto create(@Valid @RequestBody ContractDto dto) {
        // 1) Valider dates
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date de début doit être antérieure ou égale à la date de fin.");
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2) Construire l’entité
        Contract c = new Contract();
        c.setContractNumber(dto.getContractNumber());
        c.setName(dto.getName());
        c.setStartDate(dto.getStartDate());
        c.setEndDate(dto.getEndDate());
        c.setAmount(dto.getAmount());
        c.setCurrency(dto.getCurrency());
        c.setContractType(dto.getContractType());
        c.setPaymentFrequency(dto.getPaymentFrequency());
        c.setAutoRenew(dto.isAutoRenew());
        c.setProviderContact(dto.getProviderContact());
        c.setNotes(dto.getNotes());
        c.setOrganization(currentUser.getOrganization());

        // 3) Déterminer le statut
        if (dto.getEndDate().isBefore(LocalDate.now())) {
            c.setStatus(Contract.Status.RESILIE);
        } else {
            c.setStatus(Contract.Status.ACTIF);
        }

        Contract saved = contractService.save(c);
        return toDto(saved);
    }

    /**
     * PUT /api/contracts/{id}
     * Met à jour un contrat (sans statut direct), valide aussi startDate <= endDate.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContractDto> update(@PathVariable Long id,
                                              @Valid @RequestBody ContractDto dto) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date de début doit être antérieure ou égale à la date de fin.");
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long orgId = currentUser.getOrganization().getId();

        return contractService.findByIdAndOrganizationId(id, orgId).map(existing -> {
            existing.setContractNumber(dto.getContractNumber());
            existing.setName(dto.getName());
            existing.setStartDate(dto.getStartDate());
            existing.setEndDate(dto.getEndDate());
            existing.setAmount(dto.getAmount());
            existing.setCurrency(dto.getCurrency());
            existing.setContractType(dto.getContractType());
            existing.setPaymentFrequency(dto.getPaymentFrequency());
            existing.setAutoRenew(dto.isAutoRenew());
            existing.setProviderContact(dto.getProviderContact());
            existing.setNotes(dto.getNotes());

            // Recalculer le statut
            if (dto.getEndDate().isBefore(LocalDate.now())) {
                existing.setStatus(Contract.Status.RESILIE);
            } else if (existing.getStatus() == Contract.Status.RESILIE) {
                // Si antérieurement “RESILIE” et qu’on recule la date, reste “RESILIE”
            } else {
                existing.setStatus(Contract.Status.ACTIF);
            }

            Contract saved = contractService.save(existing);
            return ResponseEntity.ok(toDto(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/contracts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long orgId = currentUser.getOrganization().getId();
        boolean deleted = contractService.deleteByIdAndOrganizationId(id, orgId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * POST /api/contracts/{id}/terminate
     */
    @PostMapping("/{id}/terminate")
    public ResponseEntity<Void> terminate(@PathVariable Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long orgId = currentUser.getOrganization().getId();
        return contractService.findByIdAndOrganizationId(id, orgId).map(existing -> {
            existing.setStatus(Contract.Status.RESILIE);
            contractService.save(existing);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/contracts/{id}/reactivate
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<String> reactivate(@PathVariable Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long orgId = currentUser.getOrganization().getId();
        return contractService.findByIdAndOrganizationId(id, orgId).map(existing -> {
            if (existing.getEndDate().isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body("Impossible de réactiver : date de fin passée.");
            }
            existing.setStatus(Contract.Status.ACTIF);
            contractService.save(existing);
            return ResponseEntity.ok("Contrat réactivé");
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/contracts/ocr
     * Reçoit un fichier (PDF/JPG/PNG/etc.), renvoie un ContractDto avec
     * tous les champs tentativement extraits par OCR.
     */
    @PostMapping("/ocr")
    public ResponseEntity<ContractDto> ocrExtract(@RequestParam("file") MultipartFile file) {
        try {
            ContractDto dto = ocrService.parseContract(file);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Erreur OCR : " + e.getMessage());
        }
    }

    /**
     * Conversion interne entité → DTO
     */
    private ContractDto toDto(Contract c) {
        ContractDto dto = new ContractDto();
        dto.setId(c.getId());
        dto.setContractNumber(c.getContractNumber());
        dto.setName(c.getName());
        dto.setStartDate(c.getStartDate());
        dto.setEndDate(c.getEndDate());
        dto.setAmount(c.getAmount());
        dto.setCurrency(c.getCurrency());
        dto.setContractType(c.getContractType());
        dto.setPaymentFrequency(c.getPaymentFrequency());
        dto.setAutoRenew(c.isAutoRenew());
        dto.setProviderContact(c.getProviderContact());
        dto.setNotes(c.getNotes());
        dto.setStatus(c.getStatus().name());
        return dto;
    }
}
