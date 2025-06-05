// src/main/java/com/admineasy/dto/OrganizationSettingsDto.java
package com.admineasy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSettingsDto {
    private boolean reminder2Months;
    private boolean reminder1Month;
    private boolean reminder2Weeks;
    private boolean reminder1Week;
}
