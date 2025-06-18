package org.example.trainingservice.enums;

import lombok.Getter;

@Getter
public enum GroupeStatusEnums {
    DRAFT("Brouillon"),
    APPROVED("Validé");

    private final String description;

    GroupeStatusEnums(String description) {
        this.description = description;
    }
}