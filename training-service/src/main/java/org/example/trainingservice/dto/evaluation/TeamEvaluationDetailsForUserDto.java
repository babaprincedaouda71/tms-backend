package org.example.trainingservice.dto.evaluation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class TeamEvaluationDetailsForUserDto {
    private Long id;
    private String name;
    private String position;
    private String groupe;
    private Integer progress;
    private String status;
    private Boolean isSentToManager;
    private Boolean isSentToAdmin;
}