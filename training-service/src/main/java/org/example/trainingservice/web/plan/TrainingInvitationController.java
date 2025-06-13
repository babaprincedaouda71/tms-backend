package org.example.trainingservice.web.plan;

import org.example.trainingservice.dto.plan.SendInvitationDto;
import org.example.trainingservice.service.plan.TrainingInvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plan/trainings/invitations")
public class TrainingInvitationController {
    private final TrainingInvitationService trainingInvitationService;

    public TrainingInvitationController(TrainingInvitationService trainingInvitationService) {
        this.trainingInvitationService = trainingInvitationService;
    }

    @GetMapping("/get/all/{groupId}")
    public ResponseEntity<?> getInvitations(@PathVariable Long groupId) {
        return trainingInvitationService.getInvitations(groupId);
    }

    @PostMapping("/send-invitations/{groupId}")
    public ResponseEntity<?> sendInvitations(@PathVariable Long groupId, @RequestBody SendInvitationDto sendInvitationDto) {
        return trainingInvitationService.sendInvitations(groupId, sendInvitationDto);
    }
}