package org.example.trainingservice.web.plan;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.dto.evaluation.Participant;
import org.example.trainingservice.dto.plan.evaluation.AddGroupeEvaluationDto;
import org.example.trainingservice.dto.plan.evaluation.GroupeEvaluationDto;
import org.example.trainingservice.dto.plan.evaluation.UpdateGroupeEvaluationStatusDto;
import org.example.trainingservice.dto.plan.synthesisFile.EvaluationSyntheseDto;
import org.example.trainingservice.service.plan.evaluation.GroupeEvaluationService;
import org.example.trainingservice.service.plan.synthesisFile.EvaluationStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/plan/groupes/evaluations")
public class GroupeEvaluationController {
    private final GroupeEvaluationService groupeEvaluationService;
    private final EvaluationStatsService evaluationStatsService;

    public GroupeEvaluationController(GroupeEvaluationService groupeEvaluationService, EvaluationStatsService evaluationStatsService) {
        this.groupeEvaluationService = groupeEvaluationService;
        this.evaluationStatsService = evaluationStatsService;
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

    /**
     * Get groupe evaluation infos for f4
     */
    @GetMapping("/get/groupe-evaluation/{groupeEvaluationId}")
    public ResponseEntity<?> getGroupeEvaluationForQuestionnaire(@PathVariable UUID groupeEvaluationId) {
        return groupeEvaluationService.getGroupeEvaluationForQuestionnaire(groupeEvaluationId);
    }

    /**
     * Récupérer le token pour le qr code
     */
    @GetMapping("/get/qr-tokens/{groupeEvaluationId}")
    public ResponseEntity<?> getQRTokensForEvaluation(@PathVariable UUID groupeEvaluationId) {
        return groupeEvaluationService.getQRTokensForEvaluation(groupeEvaluationId);
    }

    /**
     * Générer la fiche de synthèse d'évaluation
     * GET /api/plan/groupes/evaluations/synthese/{groupeEvaluationId}
     */
    @GetMapping("/synthese/{groupeEvaluationId}")
    public ResponseEntity<?> generateEvaluationSynthese(@PathVariable UUID groupeEvaluationId) {
        try {
            log.info("Génération de la synthèse d'évaluation pour: {}", groupeEvaluationId);

            EvaluationSyntheseDto synthese = evaluationStatsService.generateEvaluationSynthese(groupeEvaluationId);

            return ResponseEntity.ok(synthese);

        } catch (Exception e) {
            log.error("Erreur lors de la génération de la synthèse: {}", groupeEvaluationId, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de la génération de la synthèse",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Récupérer les réponses d'un participant à une évaluation de groupe
     */
    @GetMapping("/responses/{participantId}/{groupeEvaluationId}")
    public ResponseEntity<?> getParticipantResponses(
            @PathVariable Long participantId,
            @PathVariable UUID groupeEvaluationId) {
        return groupeEvaluationService.getParticipantResponses(participantId, groupeEvaluationId);
    }
}