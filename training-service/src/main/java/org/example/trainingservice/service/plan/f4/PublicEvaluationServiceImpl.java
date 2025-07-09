package org.example.trainingservice.service.plan.f4;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.client.users.AuthServiceClient;
import org.example.trainingservice.dto.evaluation.Participant;
import org.example.trainingservice.dto.plan.f4.*;
import org.example.trainingservice.entity.campaign.Question;
import org.example.trainingservice.entity.campaign.Questionnaire;
import org.example.trainingservice.entity.campaign.UserResponse;
import org.example.trainingservice.entity.plan.TrainingGroupe;
import org.example.trainingservice.entity.plan.evaluation.GroupeEvaluation;
import org.example.trainingservice.entity.plan.f4.EvaluationQRToken;
import org.example.trainingservice.enums.EvaluationSource;
import org.example.trainingservice.enums.GroupeEvaluationStatusEnums;
import org.example.trainingservice.repository.plan.TrainingGroupeRepository;
import org.example.trainingservice.repository.plan.evaluation.GroupeEvaluationRepo;
import org.example.trainingservice.repository.plan.f4.EvaluationQRTokenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PublicEvaluationServiceImpl implements PublicEvaluationService {
    private final GroupeEvaluationRepo groupeEvaluationRepo;
    private final EvaluationQRTokenRepository qrTokenRepository;
    private final AuthServiceClient authServiceClient;
    private final TrainingGroupeRepository trainingGroupeRepository;

    public PublicEvaluationServiceImpl(GroupeEvaluationRepo groupeEvaluationRepo, EvaluationQRTokenRepository qrTokenRepository, AuthServiceClient authServiceClient, TrainingGroupeRepository trainingGroupeRepository) {
        this.groupeEvaluationRepo = groupeEvaluationRepo;
        this.qrTokenRepository = qrTokenRepository;
        this.authServiceClient = authServiceClient;
        this.trainingGroupeRepository = trainingGroupeRepository;
    }

    @Override
    public ResponseEntity<QREvaluationScanResponseDto> scanEvaluationQR(String token) {
        try {
            log.info("Scanning evaluation QR code: {}", token);

            // 1. Récupérer le token QR
            Optional<EvaluationQRToken> qrTokenOpt = qrTokenRepository.findByToken(token);

            if (qrTokenOpt.isEmpty()) {
                return ResponseEntity.ok(QREvaluationScanResponseDto.builder()
                        .valid(false)
                        .message("QR Code invalide ou expiré")
                        .errorCode("INVALID_TOKEN")
                        .build());
            }

            EvaluationQRToken qrToken = qrTokenOpt.get();

            // 2. Vérifier la validité du token
            if (!qrToken.isValid()) {
                String errorMessage = qrToken.getIsUsed() ?
                        "Ce questionnaire a déjà été rempli" :
                        "Ce QR Code a expiré";
                String errorCode = qrToken.getIsUsed() ? "ALREADY_USED" : "EXPIRED";

                return ResponseEntity.ok(QREvaluationScanResponseDto.builder()
                        .valid(false)
                        .message(errorMessage)
                        .errorCode(errorCode)
                        .build());
            }

            // 3. Récupérer l'évaluation de groupe
            GroupeEvaluation groupeEvaluation = groupeEvaluationRepo.findById(qrToken.getGroupeEvaluationId())
                    .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

            // 4. Vérifier que l'évaluation est publiée
            if (groupeEvaluation.getStatus() != GroupeEvaluationStatusEnums.PUBLISHED) {
                return ResponseEntity.ok(QREvaluationScanResponseDto.builder()
                        .valid(false)
                        .message("Cette évaluation n'est pas encore disponible")
                        .errorCode("NOT_PUBLISHED")
                        .build());
            }

            // 5. Récupérer les informations du participant
            ParticipantInfoDto participantInfo = ParticipantInfoDto.builder()
                    .participantId(qrToken.getParticipantId())
                    .fullName(qrToken.getParticipantFullName())
                    .build();

            // 6. Récupérer les informations de formation
            TrainingInfoDto trainingInfo = getTrainingInfo(groupeEvaluation.getGroupeId());

            // 7. Convertir le questionnaire
            QuestionnaireDto questionnaireDto = convertQuestionnaireToDto(groupeEvaluation.getQuestionnaire());

            // 8. Construire le formulaire d'évaluation
            EvaluationFormDto evaluationForm = EvaluationFormDto.builder()
                    .token(token)
                    .groupeEvaluationId(groupeEvaluation.getId())
                    .evaluationLabel(groupeEvaluation.getLabel())
                    .evaluationType(groupeEvaluation.getType())
                    .participant(participantInfo)
                    .training(trainingInfo)
                    .questionnaire(questionnaireDto)
                    .build();

            return ResponseEntity.ok(QREvaluationScanResponseDto.builder()
                    .valid(true)
                    .message("Accès autorisé au questionnaire d'évaluation")
                    .evaluationForm(evaluationForm)
                    .build());

        } catch (Exception e) {
            log.error("Error scanning evaluation QR code: {}", token, e);
            return ResponseEntity.ok(QREvaluationScanResponseDto.builder()
                    .valid(false)
                    .message("Erreur lors du scan du QR code")
                    .errorCode("SCAN_ERROR")
                    .build());
        }
    }

    @Override
    public ResponseEntity<SubmitResponseResultDto> submitEvaluationResponses(SubmitEvaluationResponsesDto request) {
        return null;
    }

    @Override
    @Transactional
    public void generateQRTokensForEvaluation(UUID groupeEvaluationId) {
        try {
            log.info("Generating QR tokens for evaluation: {}", groupeEvaluationId);

            GroupeEvaluation groupeEvaluation = groupeEvaluationRepo.findById(groupeEvaluationId)
                    .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

            // Supprimer les anciens tokens s'ils existent
            qrTokenRepository.deleteByGroupeEvaluationId(groupeEvaluationId);

            // Récupérer les infos des participants
            List<Participant> participants = authServiceClient.getParticipants(groupeEvaluation.getParticipantIds());

            // Créer une map pour retrouver rapidement les infos du participant
            Map<Long, Participant> participantMap = participants.stream()
                    .collect(Collectors.toMap(Participant::getId, Function.identity()));

            // Générer un token pour chaque participant
            List<EvaluationQRToken> tokens = groupeEvaluation.getParticipantIds().stream()
                    .map(participantId -> {
                        Participant participant = participantMap.get(participantId);
                        String fullName = participant.getFirstName() + " " + participant.getLastName();

                        return EvaluationQRToken.builder()
                                .token(UUID.randomUUID().toString())
                                .participantId(participantId)
                                .participantFullName(fullName)
                                .groupeEvaluationId(groupeEvaluationId)
                                .companyId(groupeEvaluation.getCompanyId())
                                .build();
                    })
                    .collect(Collectors.toList());


            qrTokenRepository.saveAll(tokens);

            log.info("Generated {} QR tokens for evaluation {}", tokens.size(), groupeEvaluationId);

        } catch (Exception e) {
            log.error("Error generating QR tokens for evaluation: {}", groupeEvaluationId, e);
            throw new RuntimeException("Erreur lors de la génération des tokens QR", e);
        }
    }


    // ====================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ====================

    public ParticipantInfoDto getParticipantInfo(Long participantId) {
        try {
            // Utiliser l'API existante pour récupérer les détails du participant
            List<Participant> participants = authServiceClient.getParticipants(List.of(participantId));

            if (!participants.isEmpty()) {
                Participant participant = participants.get(0);
                return ParticipantInfoDto.builder()
                        .participantId(participant.getId())
                        .fullName(participant.getFirstName() + " " + participant.getLastName())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Could not fetch participant details for ID: {}", participantId, e);
        }

        // Fallback si l'API ne répond pas
        return ParticipantInfoDto.builder()
                .participantId(participantId)
                .fullName("Participant")
                .build();
    }

    private TrainingInfoDto getTrainingInfo(Long groupeId) {
        Optional<TrainingGroupe> groupeOpt = trainingGroupeRepository.findById(groupeId);

        if (groupeOpt.isPresent()) {
            TrainingGroupe groupe = groupeOpt.get();
            return TrainingInfoDto.builder()
                    .groupName(groupe.getName())
                    .trainingTheme(groupe.getTraining().getTheme())
                    .location(groupe.getLocation())
                    .city(groupe.getCity())
                    .build();
        }

        return TrainingInfoDto.builder()
                .groupName("Groupe de formation")
                .trainingTheme("Formation")
                .location("")
                .city("")
                .build();
    }

    private QuestionnaireDto convertQuestionnaireToDto(Questionnaire questionnaire) {
        List<QuestionDto> questionDtos = questionnaire.getQuestions().stream()
                .map(question -> QuestionDto.builder()
                        .questionId(question.getId())
                        .type(question.getType())
                        .text(question.getText())
                        .comment(question.getComment())
                        .options(question.getOptions())
                        .levels(question.getLevels())
                        .required(true) // Toutes les questions sont obligatoires selon vos préférences
                        .build())
                .collect(Collectors.toList());

        return QuestionnaireDto.builder()
                .questionnaireId(questionnaire.getId())
                .title(questionnaire.getTitle())
                .description(questionnaire.getDescription())
                .questions(questionDtos)
                .build();
    }

    private boolean validateAllQuestionsAnswered(List<Question> questions, List<EvaluationResponseDto> responses) {
        Set<UUID> answeredQuestionIds = responses.stream()
                .map(EvaluationResponseDto::getQuestionId)
                .collect(Collectors.toSet());

        return questions.stream()
                .allMatch(question -> answeredQuestionIds.contains(question.getId()));
    }

    private UserResponse convertToUserResponse(EvaluationResponseDto responseDto,
                                               EvaluationQRToken qrToken,
                                               GroupeEvaluation groupeEvaluation) {
        return UserResponse.builder()
                .companyId(qrToken.getCompanyId())
                .userId(qrToken.getParticipantId())
                .groupeEvaluationId(qrToken.getGroupeEvaluationId())
                .questionnaireId(groupeEvaluation.getQuestionnaire().getId())
                .questionId(responseDto.getQuestionId())
                .responseType(responseDto.getResponseType())
                .textResponse(responseDto.getTextResponse())
                .commentResponse(responseDto.getCommentResponse())
                .scoreResponse(responseDto.getScoreResponse())
                .ratingResponse(responseDto.getRatingResponse())
                .multipleChoiceResponse(responseDto.getMultipleChoiceResponse())
                .singleChoiceResponse(responseDto.getSingleChoiceResponse())
                .singleLevelChoiceResponse(responseDto.getSingleLevelChoiceResponse())
                .status("COMPLETED")
                .progression(100)
                .startDate(LocalDate.now())
                .lastModifiedDate(LocalDate.now())
                .isSentToManager(false)
                .isSentToAdmin(false)
                .evaluationSource(EvaluationSource.GROUPE_EVALUATION)
                .build();
    }
}