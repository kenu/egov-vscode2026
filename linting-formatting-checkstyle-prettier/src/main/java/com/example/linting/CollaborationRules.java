package com.example.linting;

import java.util.List;

public class CollaborationRules {

    public List<String> javaRules() {
        return List.of("AvoidStarImport", "NeedBraces", "UnusedImports", "LineLength");
    }

    public List<String> documentRules() {
        return List.of("README.md", "src/main/resources/**/*.yml");
    }
}
