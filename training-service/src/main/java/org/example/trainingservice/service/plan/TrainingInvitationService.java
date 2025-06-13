package org.example.trainingservice.service.plan;

import org.example.trainingservice.dto.plan.SendInvitationDto;
import org.example.trainingservice.entity.plan.TrainingGroupe;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface TrainingInvitationService {
    ResponseEntity<?> getInvitations(Long groupId);

    ResponseEntity<?> sendInvitations(Long groupId, SendInvitationDto sendInvitationDto);

    void createTrainingInvitation(TrainingGroupe trainingGroupe, Set<Long> userGroupIds);
}