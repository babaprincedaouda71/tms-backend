package org.example.trainingservice.utils;

import org.example.trainingservice.dto.plan.TrainingInvitationDto;
import org.example.trainingservice.entity.plan.TrainingInvitation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TrainingInvitationUtilMethods {
    public static List<TrainingInvitationDto> mapToTrainingInvitationDtos(List<TrainingInvitation> byTrainingGroupeId) {
        if (byTrainingGroupeId == null || byTrainingGroupeId.isEmpty()) {
            return Collections.emptyList();
        }
        return byTrainingGroupeId.stream()
                .map(TrainingInvitationUtilMethods::mapToTrainingInvitationDto)
                .collect(Collectors.toList());
    }

    private static TrainingInvitationDto mapToTrainingInvitationDto(TrainingInvitation trainingInvitation) {
        if (trainingInvitation == null) {
            return null;
        }

        return TrainingInvitationDto.builder()
                .id(trainingInvitation.getId())
                .userFullName(trainingInvitation.getUserFullName())
                .userEmail(trainingInvitation.getUserEmail())
                .invitationDate(trainingInvitation.getInvitationDate())
                .status(trainingInvitation.getStatus())
                .build();
    }
}