package com.secogroupe.app.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportResult {
    private int imported;
    private int skipped;
    private List<String> errors;
}
