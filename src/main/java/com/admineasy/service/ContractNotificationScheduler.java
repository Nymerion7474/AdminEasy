// src/main/java/com/admineasy/service/ContractNotificationScheduler.java
package com.admineasy.service;

import com.admineasy.model.Contract;
import com.admineasy.model.Organization;
import com.admineasy.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ContractNotificationScheduler {

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private ContractService contractService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Tâche programmée qui tourne tous les jours à 02h00 du matin.
     * Elle vérifie, pour chaque organisation, si :
     * - jour J : on envoie quoiqu’il arrive
     * - + 1 semaine, 2 semaines, 1 mois, 2 mois si configurés
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkAndSendReminders() {
        LocalDate today = LocalDate.now();
        List<Organization> orgs = orgRepo.findAll();

        for (Organization org : orgs) {
            Long orgId = org.getId();

            // 1) Rappel "Jour J" (non négociable)
            sendIfMatches(org, today, "Expiration du contrat aujourd'hui.");

            // 2) Rappel 1 semaine avant
            if (org.isReminder1Week()) {
                LocalDate date1Week = today.plusWeeks(1);
                sendIfMatches(org, date1Week, "Il reste 1 semaine avant l'expiration du contrat.");
            }

            // 3) Rappel 2 semaines avant
            if (org.isReminder2Weeks()) {
                LocalDate date2Weeks = today.plusWeeks(2);
                sendIfMatches(org, date2Weeks, "Il reste 2 semaines avant l'expiration du contrat.");
            }

            // 4) Rappel 1 mois avant
            if (org.isReminder1Month()) {
                LocalDate date1Month = today.plusMonths(1);
                sendIfMatches(org, date1Month, "Il reste 1 mois avant l'expiration du contrat.");
            }

            // 5) Rappel 2 mois avant
            if (org.isReminder2Months()) {
                LocalDate date2Months = today.plusMonths(2);
                sendIfMatches(org, date2Months, "Il reste 2 mois avant l'expiration du contrat.");
            }
        }
    }

    /**
     * Vérifie si des contrats d'une org expirent exactement à la date passée,
     * et envoie les rappels (e-mail + in-app).
     */
    private void sendIfMatches(Organization org, LocalDate targetDate, String baseMessage) {
        Long orgId = org.getId();
        List<Contract> contracts = contractService.findByOrganizationIdAndEndDate(orgId, targetDate);
        for (Contract contract : contracts) {
            String message = baseMessage + " (Contrat : \"" + contract.getName() + "\" - Expiration : " + contract.getEndDate() + ")";
            // 1) Envoi e-mail
            notificationService.sendEmailReminder(orgId, contract, message);
            // 2) Envoi in-app
            notificationService.sendInAppReminder(orgId, contract, message);
        }
    }
}
