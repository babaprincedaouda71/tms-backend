package org.example.trainingservice.dto.trainingRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddMyTrainingRequestDto {
    private Integer year;

    private String domain;

    private String theme;

    private String site;

    private String department;

    private LocalDate wishDate;

    private Requester requester;

    private String objective;

    private String content;

    private String learningMode;
}