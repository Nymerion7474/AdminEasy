// src/main/java/com/admineasy/model/Contract.java
package com.admineasy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "organization")
@EqualsAndHashCode(exclude = "organization")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Numero unique du contrat (ex : “CTR-2025-001”)
     */
    @Column(unique = true, nullable = false)
    private String contractNumber;

    /**
     * Titre ou nom court
     */
    @NotNull
    private String name;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    /**
     * Montant (en gros, peut être null si pas de montant fixe)
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Devise du montant, ex “EUR”, “USD”
     */
    @Column(length = 3)
    private String currency;

    /**
     * Type de contrat (ex : “Licence logicielle”, “Maintenance”, “SLA”, etc.)
     */
    private String contractType;

    /**
     * Fréquence de paiement (“Mensuel”, “Annuel”, “Unique”, etc.)
     */
    private String paymentFrequency;

    /**
     * Auto‐renouvellement ?
     */
    private boolean autoRenew = false;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String providerContact;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    public enum Status {
        ACTIF,
        RESILIE
    }
}
