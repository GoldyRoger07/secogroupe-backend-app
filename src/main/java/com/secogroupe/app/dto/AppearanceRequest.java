package com.secogroupe.app.dto;

import lombok.Data;

@Data
public class AppearanceRequest {
    private String theme;
    private String density;
    private String fontSize;
    private String sidebarColor;
}
