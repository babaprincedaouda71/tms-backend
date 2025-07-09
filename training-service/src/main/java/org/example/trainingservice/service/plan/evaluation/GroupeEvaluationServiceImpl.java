package org.example.trainingservice.service.plan.evaluation;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.client.users.AuthServiceClient;
import org.example.trainingservice.dto.evaluation.Participant;
import org.example.trainingservice.dto.plan.evaluation.AddGroupeEvaluationDto;
import org.example.trainingservice.dto.plan.evaluation.GroupeEvaluationDto;
import org.example.trainingservice.dto.plan.evaluation.UpdateGroupeEvaluationStatusDto;
import org.example.trainingservice.entity.campaign.Questionnaire;
import org.example.trainingservice.entity.plan.Training;
import org.example.trainingservice.entity.plan.TrainingGroupe;
import org.example.trainingservice.entity.plan.evaluation.GroupeEvaluation;
import org.example.trainingservice.enums.GroupeEvaluationStatusEnums;
import org.example.trainingservice.repository.evaluation.QuestionnaireRepository;
import org.example.trainingservice.repository.plan.TrainingGroupeRepository;
import org.example.trainingservice.repository.plan.TrainingRepository;
import org.example.trainingservice.repository.plan.evaluation.GroupeEvaluationRepo;
import org.example.trainingservice.service.plan.f4.PublicEvaluationService;
import org.example.trainingservice.utils.GroupeEvaluationUtilMethods;
import org.example.trainingservice.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class GroupeEvaluationServiceImpl implements GroupeEvaluationService {
    private final GroupeEvaluationRepo groupeEvaluationRepo;
    private final TrainingGroupeRepository trainingGroupeRepository;
    private final AuthServiceClient authServiceClient;
    private final QuestionnaireRepository questionnaireRepository;
    private final TrainingRepository trainingRepository;
    private final PublicEvaluationService publicEvaluationService;

    public GroupeEvaluationServiceImpl(GroupeEvaluationRepo groupeEvaluationRepo, TrainingGroupeRepository trainingGroupeRepository, AuthServiceClient authServiceClient, QuestionnaireRepository questionnaireRepository, TrainingRepository trainingRepository, PublicEvaluationService publicEvaluationService) {
        this.groupeEvaluationRepo = groupeEvaluationRepo;
        this.trainingGroupeRepository = trainingGroupeRepository;
        this.authServiceClient = authServiceClient;
        this.questionnaireRepository = questionnaireRepository;
        this.trainingRepository = trainingRepository;
        this.publicEvaluationService = publicEvaluationService;
    }

    @Override
    public List<GroupeEvaluationDto> getAllGroupeEvaluations(UUID trainingId, Long groupId) {
        log.info("getAllGroupeEvaluations trainingId : {}, groupId : {}", trainingId, groupId);

        log.info("Fetching groupe evaluations");
        List<GroupeEvaluation> allByTrainingIdAndGroupeId = groupeEvaluationRepo.findAllByTrainingIdAndGroupeId(trainingId, groupId);

        if (allByTrainingIdAndGroupeId != null && !allByTrainingIdAndGroupeId.isEmpty()) {
            List<GroupeEvaluationDto> groupeEvaluationDtos = GroupeEvaluationUtilMethods.mapToGroupeEvaluationDtos(allByTrainingIdAndGroupeId);
            log.info("Finished fetching groupe evaluations");
            return groupeEvaluationDtos;
        }
        return Collections.emptyList();
    }

    @Override
    public List<Participant> fetchParticipants(UUID trainingId, Long groupId) {
        log.info("fetchParticipants trainingId : {}, groupId : {}", trainingId, groupId);
        Optional<TrainingGroupe> byId = trainingGroupeRepository.findById(groupId);
        if (byId.isPresent()) {
            TrainingGroupe trainingGroupe = byId.get();

            Set<Long> userGroupIds = trainingGroupe.getUserGroupIds();

            try {
                List<Participant> participants = authServiceClient.getParticipants(new ArrayList<>(userGroupIds));
                log.info("Finished fetching participants");
                return participants;
            }
            catch (Exception e) {
                log.error("Error fetching participants", e);
                return List.of();
            }
        }
        return List.of();
    }

    @Override
    public void addGroupeEvaluation(UUID trainingId, Long groupId, AddGroupeEvaluationDto addGroupeEvaluationDto) {
        log.info("addGroupeEvaluation groupId : {}", groupId);
        UUID questionnaireId = addGroupeEvaluationDto.getQuestionnaireId();
        Optional<Training> byIdAndCompanyId = trainingRepository.findByIdAndCompanyId(trainingId, SecurityUtils.getCurrentCompanyId());
        Optional<TrainingGroupe> byId = trainingGroupeRepository.findById(groupId);
        Optional<Questionnaire> byId1 = questionnaireRepository.findById(questionnaireId);
        if (byIdAndCompanyId.isPresent() && byId.isPresent() && byId1.isPresent()) {
            Questionnaire questionnaire = byId1.get();

            GroupeEvaluation groupeEvaluation = GroupeEvaluation.builder()
                    .groupeId(groupId)
                    .label(addGroupeEvaluationDto.getLabel())
                    .type(addGroupeEvaluationDto.getType())
                    .companyId(SecurityUtils.getCurrentCompanyId())
                    .creationDate(LocalDate.now())
                    .trainingId(trainingId)
                    .questionnaire(questionnaire)
                    .participantIds(addGroupeEvaluationDto.getParticipantIds())
                    .status(GroupeEvaluationStatusEnums.DRAFT)
                    .build();

            groupeEvaluationRepo.save(groupeEvaluation);
            log.info("Finished adding groupe evaluation");
        }
    }

    @Override
    public void updateStatus(UpdateGroupeEvaluationStatusDto dto) {
        log.info("updateStatus dto : {}", dto);
        UUID evaluationId = dto.getId();
        String statusString = dto.getStatus();
        if (evaluationId != null && statusString != null && !statusString.trim().isEmpty()) {
            Optional<GroupeEvaluation> byId = groupeEvaluationRepo.findById(evaluationId);

            if (byId.isPresent()) {
                GroupeEvaluation groupeEvaluation = byId.get();
                groupeEvaluation.setStatus(GroupeEvaluationStatusEnums.fromDescription(statusString));
                groupeEvaluationRepo.save(groupeEvaluation);

                if (Objects.equals(groupeEvaluation.getType(), "Formulaire F4")) {
                    try {
                        log.info("Generating QR tokens for published evaluation: {}", evaluationId);
                        publicEvaluationService.generateQRTokensForEvaluation(evaluationId);
                        log.info("QR tokens generated successfully for evaluation: {}", evaluationId);
                    } catch (Exception e) {
                        log.error("Error generating QR tokens for evaluation: {}", evaluationId, e);
                        // On peut choisir de relancer l'exception ou juste logger l'erreur
                        // selon les besoins métier
                    }
                }
                log.info("Finished updating groupe evaluation status");
            }
            else {
                log.warn("Evaluation not found with id : {}", evaluationId);
            }
        }
        else {
            log.warn("Evaluation id or status are null or empty");
        }
    }
}