package org.example.trainingservice.web.plan;

import org.example.trainingservice.dto.evaluation.Participant;
import org.example.trainingservice.dto.plan.evaluation.AddGroupeEvaluationDto;
import org.example.trainingservice.dto.plan.evaluation.GroupeEvaluationDto;
import org.example.trainingservice.dto.plan.evaluation.UpdateGroupeEvaluationStatusDto;
import org.example.trainingservice.service.plan.evaluation.GroupeEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plan/groupes/evaluations")
public class GroupeEvaluationController {
    private final GroupeEvaluationService groupeEvaluationService;

    public GroupeEvaluationController(GroupeEvaluationService groupeEvaluationService) {
        this.groupeEvaluationService = groupeEvaluationService;
    }

    /*
    * Groupe Evaluation
    * */
    @GetMapping("/get/all/{trainingId}/{groupId}")
    public ResponseEntity<?> getAllGroupeEvaluations(@PathVariable UUID trainingId, @PathVariable Long groupId) {
        List<GroupeEvaluationDto> allGroupeEvaluations = groupeEvaluationService.getAllGroupeEvaluations(trainingId, groupId);
        return ResponseEntity.ok(allGroupeEvaluations);
    }

    @GetMapping("/get/participants/{trainingId}/{groupId}")
    public ResponseEntity<?> fetchParticipants(@PathVariable UUID trainingId, @PathVariable Long groupId) {
        List<Participant> participants = groupeEvaluationService.fetchParticipants(trainingId, groupId);
        return ResponseEntity.ok(participants);
    }

    @PostMapping("/add/{trainingId}/{groupId}")
    public ResponseEntity<?> addGroupeEvaluation(@PathVariable UUID trainingId, @PathVariable Long groupId, @RequestBody AddGroupeEvaluationDto addGroupeEvaluationDto) {
        groupeEvaluationService.addGroupeEvaluation(trainingId, groupId, addGroupeEvaluationDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update-status")
    public ResponseEntity<?> updateStatus(@RequestBody UpdateGroupeEvaluationStatusDto dto) {
        groupeEvaluationService.updateStatus(dto);
        return ResponseEntity.ok().build();
    }
}