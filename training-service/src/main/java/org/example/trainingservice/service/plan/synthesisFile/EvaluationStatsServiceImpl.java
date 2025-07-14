package org.example.trainingservice.service.plan.synthesisFile;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.dto.plan.synthesisFile.EvaluationSyntheseDto;
import org.example.trainingservice.dto.plan.synthesisFile.QuestionStatsDto;
import org.example.trainingservice.entity.campaign.Question;
import org.example.trainingservice.entity.campaign.UserResponse;
import org.example.trainingservice.entity.plan.evaluation.GroupeEvaluation;
import org.example.trainingservice.repository.evaluation.UserResponseRepository;
import org.example.trainingservice.repository.plan.evaluation.GroupeEvaluationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EvaluationStatsServiceImpl implements EvaluationStatsService {

    private final GroupeEvaluationRepo groupeEvaluationRepo;
    private final UserResponseRepository userResponseRepository;

    public EvaluationStatsServiceImpl(GroupeEvaluationRepo groupeEvaluationRepo,
                                      UserResponseRepository userResponseRepository) {
        this.groupeEvaluationRepo = groupeEvaluationRepo;
        this.userResponseRepository = userResponseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationSyntheseDto generateEvaluationSynthese(UUID groupeEvaluationId) {
        try {
            log.info("Génération de la synthèse pour l'évaluation: {}", groupeEvaluationId);

            // 1. Récupérer l'évaluation de groupe avec le questionnaire
            GroupeEvaluation groupeEvaluation = groupeEvaluationRepo.findById(groupeEvaluationId)
                    .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

            // 2. Vérifier que l'évaluation est complète
            if (!isEvaluationComplete(groupeEvaluation)) {
                throw new RuntimeException("L'évaluation n'est pas encore complète");
            }

            // 3. Récupérer toutes les réponses pour cette évaluation
            List<UserResponse> allResponses = userResponseRepository
                    .findByGroupeEvaluationIdAndUserIdIn(
                            groupeEvaluationId,
                            groupeEvaluation.getParticipantIds()
                    );

            // 4. Calculer les statistiques par question
            List<QuestionStatsDto> questionStats = calculateQuestionStats(
                    groupeEvaluation.getQuestionnaire().getQuestions(),
                    allResponses,
                    groupeEvaluation.getParticipantIds().size()
            );

            // 5. Construire le DTO de synthèse
            return EvaluationSyntheseDto.builder()
                    .evaluationId(groupeEvaluationId)
                    .evaluationLabel(groupeEvaluation.getLabel())
                    .questionnaireTitle(groupeEvaluation.getQuestionnaire().getTitle())
                    .questionnaireDescription(groupeEvaluation.getQuestionnaire().getDescription())
                    .totalParticipants(groupeEvaluation.getParticipantIds().size())
                    .totalResponses(getUniqueRespondentsCount(allResponses))
                    .completionPercentage(calculateCompletionPercentage(groupeEvaluation, allResponses))
                    .questionStats(questionStats)
                    .generationDate(new Date())
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la génération de la synthèse: {}", groupeEvaluationId, e);
            throw new RuntimeException("Erreur lors de la génération de la synthèse: " + e.getMessage());
        }
    }

    /**
     * Vérifier si l'évaluation est complète (tous les participants ont répondu)
     */
    private boolean isEvaluationComplete(GroupeEvaluation groupeEvaluation) {
        List<UserResponse> responses = userResponseRepository
                .findByGroupeEvaluationIdAndUserIdIn(
                        groupeEvaluation.getId(),
                        groupeEvaluation.getParticipantIds()
                );

        // Compter les participants uniques qui ont répondu
        Set<Long> respondents = responses.stream()
                .map(UserResponse::getUserId)
                .collect(Collectors.toSet());

        return respondents.size() == groupeEvaluation.getParticipantIds().size();
    }

    /**
     * Calculer les statistiques pour chaque question
     */
    private List<QuestionStatsDto> calculateQuestionStats(List<Question> questions,
                                                          List<UserResponse> allResponses,
                                                          int totalParticipants) {
        return questions.stream()
                .map(question -> calculateSingleQuestionStats(question, allResponses, totalParticipants))
                .collect(Collectors.toList());
    }

    /**
     * Calculer les statistiques pour une question donnée
     */
    private QuestionStatsDto calculateSingleQuestionStats(Question question,
                                                          List<UserResponse> allResponses,
                                                          int totalParticipants) {
        // Filtrer les réponses pour cette question
        List<UserResponse> questionResponses = allResponses.stream()
                .filter(response -> response.getQuestionId().equals(question.getId()))
                .collect(Collectors.toList());

        // Calculer les statistiques selon le type de question
        Map<String, Double> optionPercentages = new LinkedHashMap<>();

        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            // Questions à choix unique avec options
            optionPercentages = calculateOptionPercentages(
                    question.getOptions(),
                    questionResponses,
                    totalParticipants
            );
        } else if (question.getLevels() != null && !question.getLevels().isEmpty()) {
            // Questions avec niveaux
            optionPercentages = calculateOptionPercentages(
                    question.getLevels(),
                    questionResponses,
                    totalParticipants
            );
        }

        return QuestionStatsDto.builder()
                .questionId(question.getId())
                .questionText(question.getText())
                .questionType(question.getType())
                .totalResponses(questionResponses.size())
                .optionPercentages(optionPercentages)
                .build();
    }

    /**
     * Calculer les pourcentages pour chaque option
     */
    private Map<String, Double> calculateOptionPercentages(List<String> options,
                                                           List<UserResponse> responses,
                                                           int totalParticipants) {
        Map<String, Double> percentages = new LinkedHashMap<>();

        // Compter les réponses pour chaque option
        Map<String, Long> optionCounts = responses.stream()
                .filter(response -> response.getSingleChoiceResponse() != null)
                .collect(Collectors.groupingBy(
                        UserResponse::getSingleChoiceResponse,
                        Collectors.counting()
                ));

        // Calculer les pourcentages
        for (String option : options) {
            long count = optionCounts.getOrDefault(option, 0L);
            double percentage = totalParticipants > 0 ?
                    (double) count / totalParticipants * 100 : 0.0;

            // Arrondir à 1 décimale
            percentage = BigDecimal.valueOf(percentage)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();

            percentages.put(option, percentage);
        }

        return percentages;
    }

    /**
     * Compter le nombre de participants uniques qui ont répondu
     */
    private int getUniqueRespondentsCount(List<UserResponse> responses) {
        return (int) responses.stream()
                .map(UserResponse::getUserId)
                .distinct()
                .count();
    }

    /**
     * Calculer le pourcentage de completion global
     */
    private double calculateCompletionPercentage(GroupeEvaluation groupeEvaluation,
                                                 List<UserResponse> allResponses) {
        int totalParticipants = groupeEvaluation.getParticipantIds().size();
        int uniqueRespondents = getUniqueRespondentsCount(allResponses);

        return totalParticipants > 0 ?
                (double) uniqueRespondents / totalParticipants * 100 : 0.0;
    }
}