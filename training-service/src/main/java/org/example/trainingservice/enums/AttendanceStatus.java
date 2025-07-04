package org.example.trainingservice.enums;

import lombok.Getter;

@Getter
public enum AttendanceStatus {
    ABSENT("absent"),
    PRESENT("présent");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}