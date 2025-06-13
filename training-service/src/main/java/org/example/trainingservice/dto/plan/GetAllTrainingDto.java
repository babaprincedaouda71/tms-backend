package org.example.trainingservice.dto.plan;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class GetAllTrainingDto {
    private UUID id;

    private String theme;

    private String creationDate;

    private String type;

    private String ocf;

    private Boolean csf;

    private BigDecimal budget;

    private String status;
}