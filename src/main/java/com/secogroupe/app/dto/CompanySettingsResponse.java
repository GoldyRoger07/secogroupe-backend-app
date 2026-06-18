package com.secogroupe.app.dto;

import lombok.Data;

@Data
public class CompanySettingsResponse {
    private Long id;
    private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String website;
    private String address;
    private String timezone;
    private String dateFormat;
    private String language;
}
