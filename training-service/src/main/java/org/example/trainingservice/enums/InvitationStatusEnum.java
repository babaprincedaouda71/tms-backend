package org.example.trainingservice.enums;

public enum InvitationStatusEnum {
    NOT_SENT("Non envoyée"),
    PENDING("En attente"),
    ACCEPTED("Acceptée"),
    DECLINED("Refusée"),
    EXPIRED("Expirée"),
    CANCELLED("Annulée");

    private final String description;

    InvitationStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}