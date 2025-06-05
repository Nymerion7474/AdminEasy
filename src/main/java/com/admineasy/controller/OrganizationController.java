// src/main/java/com/admineasy/controller/OrganizationController.java
package com.admineasy.controller;

import com.admineasy.dto.OrganizationSettingsDto;
import com.admineasy.model.Organization;
import com.admineasy.model.User;
import com.admineasy.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/orgs")
public class OrganizationController {

    @Autowired
    private OrganizationRepository orgRepo;

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody OrganizationSettingsDto dto) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long orgId = currentUser.getOrganization().getId();
        Optional<Organization> optOrg = orgRepo.findById(orgId);
        if (optOrg.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Organization org = optOrg.get();
        org.setReminder2Months(dto.isReminder2Months());
        org.setReminder1Month(dto.isReminder1Month());
        org.setReminder2Weeks(dto.isReminder2Weeks());
        org.setReminder1Week(dto.isReminder1Week());

        orgRepo.save(org);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings")
    public ResponseEntity<OrganizationSettingsDto> getSettings() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long orgId = currentUser.getOrganization().getId();
        Optional<Organization> optOrg = orgRepo.findById(orgId);
        if (optOrg.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Organization org = optOrg.get();
        OrganizationSettingsDto dto = new OrganizationSettingsDto(
                org.isReminder2Months(),
                org.isReminder1Month(),
                org.isReminder2Weeks(),
                org.isReminder1Week()
        );
        return ResponseEntity.ok(dto);
    }
}
