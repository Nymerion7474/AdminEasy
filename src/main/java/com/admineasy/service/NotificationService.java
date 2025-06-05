// src/main/java/com/admineasy/service/NotificationService.java
package com.admineasy.service;

import com.admineasy.model.Contract;
import com.admineasy.model.Organization;
import com.admineasy.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private ContractService contractService;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envoie un e-mail de notification à l’admin de l’organisation
     *
     * @param orgId    id de l’org
     * @param contract contrat concerné
     * @param message  texte à mettre dans l’e-mail
     */
    public void sendEmailReminder(Long orgId, Contract contract, String message) {
        Organization org = orgRepo.findById(orgId).orElse(null);
        if (org == null) return;

        String to = org.getAdminEmail();
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Rappel contrat : " + contract.getName());
        mail.setText(message);
        mailSender.send(mail);
    }

    /**
     * Envoie un rappel in-app (on pourrait stocker un flag dans la base ou appeler un websocket,
     * ici on simule juste un log ou on pourrait enregistrer une notification dans une table dédiée).
     *
     * @param orgId    id de l’org
     * @param contract contrat concerné
     * @param message  texte à afficher in-app
     */
    public void sendInAppReminder(Long orgId, Contract contract, String message) {
        // Pour simplifier, on se contente ici de logger.
        // Dans une V2, tu pourrais ajouter une entité Notification (orgId, contractId, message, date, lu/non-lu).
        System.out.println("[In-App Reminder] Org “" + orgId + "” – Contrat “" + contract.getName()
                + "” : " + message);
    }
}
