package com.secogroupe.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanySettingsRequest {
    @NotBlank private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String website;
    private String address;
    private String timezone;
    private String dateFormat;
    private String language;
}
