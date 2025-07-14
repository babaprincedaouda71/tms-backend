package org.example.trainingservice.dto.plan.synthesisFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationSyntheseDto {
    private UUID evaluationId;
    private String evaluationLabel;
    private String questionnaireTitle;
    private String questionnaireDescription;
    private int totalParticipants;
    private int totalResponses;
    private double completionPercentage;
    private List<QuestionStatsDto> questionStats;
    private Date generationDate;
}