package org.example.trainingservice.service.evaluations;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.cacheService.UserCacheService;
import org.example.trainingservice.client.users.AuthServiceClient;
import org.example.trainingservice.dto.evaluation.*;
import org.example.trainingservice.entity.Groupe;
import org.example.trainingservice.entity.Need;
import org.example.trainingservice.entity.campaign.CampaignEvaluation;
import org.example.trainingservice.entity.campaign.Question;
import org.example.trainingservice.entity.campaign.Questionnaire;
import org.example.trainingservice.entity.campaign.UserResponse;
import org.example.trainingservice.enums.GroupeStatusEnums;
import org.example.trainingservice.enums.NeedSource;
import org.example.trainingservice.enums.NeedStatusEnums;
import org.example.trainingservice.repository.NeedRepository;
import org.example.trainingservice.repository.evaluation.CampaignEvaluationRepository;
import org.example.trainingservice.repository.evaluation.QuestionnaireRepository;
import org.example.trainingservice.repository.evaluation.UserResponseRepository;
import org.example.trainingservice.utils.EvaluationUtilMethods;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TeamEvaluationsServiceImpl implements TeamEvaluationsService {
    private final QuestionnaireRepository questionnaireRepository;
    private final AuthServiceClient authServiceClient;
    private final CampaignEvaluationRepository campaignEvaluationRepository;
    private final UserResponseRepository userResponseRepository;
    private final NeedRepository needRepository;
    private final UserCacheService userCacheService;

    public TeamEvaluationsServiceImpl(QuestionnaireRepository questionnaireRepository, AuthServiceClient authServiceClient, CampaignEvaluationRepository campaignEvaluationRepository, UserResponseRepository userResponseRepository, NeedRepository needRepository, UserCacheService userCacheService) {
        this.questionnaireRepository = questionnaireRepository;
        this.authServiceClient = authServiceClient;
        this.campaignEvaluationRepository = campaignEvaluationRepository;
        this.userResponseRepository = userResponseRepository;
        this.needRepository = needRepository;
        this.userCacheService = userCacheService;
    }

//    @Override
//    public ResponseEntity<List<GetTeamEvaluationsDto>> getTeamEvaluations(Long managerId) {
//        List<Long> myTeamIds = authServiceClient.getMyTeam(managerId);
//        List<GetTeamEvaluationsDto> teamEvaluationDtos = new ArrayList<>();
//
//        if (myTeamIds != null && !myTeamIds.isEmpty()) {
//            List<CampaignEvaluation> campaignEvaluations = campaignEvaluationRepository.findByAnyParticipantIdInAndStatus(myTeamIds, "Publiée");
//
//            for (CampaignEvaluation campaignEvaluation : campaignEvaluations) {
//                for (Questionnaire questionnaire : campaignEvaluation.getQuestionnaires()) {
//                    // Récupérer les réponses des membres de l'équipe pour CE QUESTIONNAIRE
//                    List<UserResponse> teamResponsesForQuestionnaire = userResponseRepository.findByQuestionnaireIdAndUserIdIn(questionnaire.getId(), myTeamIds);
//
//                    // Calculer l'état global pour ce questionnaire pour l'équipe
//                    String globalStatus = calculateGlobalStatusForTeam(teamResponsesForQuestionnaire);
//
//                    // Calculer l'avancement global moyen pour ce questionnaire pour l'équipe
//                    Integer globalProgress = calculateGlobalProgressForTeam(teamResponsesForQuestionnaire);
//
//                    GetTeamEvaluationsDto dto = GetTeamEvaluationsDto.builder()
//                            .id(questionnaire.getId())
//                            .title(questionnaire.getTitle())
//                            .status(globalStatus)
//                            .creationDate(questionnaire.getCreationDate().toString())
//                            .type(questionnaire.getType())
//                            .participants(myTeamIds.size()) // Nombre total de membres de l'équipe
//                            .progress(globalProgress)
//                            .build();
//
//                    teamEvaluationDtos.add(dto);
//                }
//            }
//        }
//
//        return ResponseEntity.ok(teamEvaluationDtos);
//    }
//
//    private String calculateGlobalStatusForTeam(List<UserResponse> userResponses) {
//        if (userResponses.isEmpty()) {
//            return "En attente";
//        }
//        boolean allTerminated = userResponses.stream().allMatch(response -> "Terminée".equalsIgnoreCase(response.getStatus()));
//        if (allTerminated) {
//            return "Terminé";
//        }
//        boolean anyInProgress = userResponses.stream().anyMatch(response -> "En cours".equalsIgnoreCase(response.getStatus()));
//        if (anyInProgress) {
//            return "En cours";
//        }
//        return "En attente";
//    }
//
//    private Integer calculateGlobalProgressForTeam(List<UserResponse> userResponses) {
//        if (userResponses.isEmpty()) {
//            return 0;
//        }
//
//        // Map pour stocker la progression maximale par utilisateur
//        Map<Long, Integer> maxProgressByUser = new HashMap<>();
//
//        // Pour chaque réponse, on garde le progrès le plus élevé par utilisateur
//        for (UserResponse response : userResponses) {
//            Long userId = response.getUserId();
//            Integer progression = response.getProgression();
//
//            if (progression != null) {
//                // Si l'utilisateur n'existe pas encore dans la map ou si sa progression actuelle est supérieure
//                if (!maxProgressByUser.containsKey(userId) || progression > maxProgressByUser.get(userId)) {
//                    maxProgressByUser.put(userId, progression);
//                }
//            }
//        }
//
//        // Calculer la moyenne des progressions maximales
//        if (maxProgressByUser.isEmpty()) {
//            return 0;
//        }
//
//        double averageProgress = maxProgressByUser.values().stream()
//                .mapToInt(Integer::intValue)
//                .average()
//                .orElse(0);
//
//        return (int) Math.round(averageProgress);
//    }
//
//
//    @Override
//    public ResponseEntity<?> getTeamEvaluationDetails(UUID questionnaireId, Long managerId) {
//        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
//                .orElseThrow(() -> new RuntimeException("Questionnaire non trouvé"));
//
//        List<Long> myTeamIds = authServiceClient.getMyTeam(managerId);
//        log.error("My team IDs : {}.", myTeamIds); // Conservez ou ajustez le logging selon vos besoins
//        List<TeamEvaluationDetailsForUserDto> participantsDetails = new ArrayList<>();
//        List<UserResponse> allTeamResponsesForQuestionnaire = userResponseRepository.findByQuestionnaireIdAndUserIdIn(questionnaireId, myTeamIds);
//
//        if (myTeamIds != null && !myTeamIds.isEmpty()) {
//            log.info("Traitement des détails d'évaluation pour le questionnaire {} pour {} participants.", questionnaireId, myTeamIds.size()); // Log amélioré
//
//            for (Long userId : myTeamIds) {
//                // Filtrer les réponses pour l'utilisateur courant
//                List<UserResponse> userResponses = allTeamResponsesForQuestionnaire.stream()
//                        .filter(response -> response.getUserId().equals(userId))
//                        .toList();
//
//                String status = "En attente"; // Statut par défaut
//                int progress = 0;             // Progression par défaut
//                int totalQuestions = questionnaire.getQuestions().size();
//                int numberOfValidResponses = 0;
//
//                // Compter le nombre de réponses valides pour cet utilisateur et ce questionnaire
//                // Cette logique est maintenant alignée avec getMyEvaluations
//                for (UserResponse userResponse : userResponses) {
//                    switch (userResponse.getResponseType()) {
//                        case "Score":
//                            if (userResponse.getScoreResponse() != null) {
//                                numberOfValidResponses++;
//                            }
//                            break;
//                        case "Notation": // Ajouté pour correspondre à getMyEvaluations
//                            if (userResponse.getRatingResponse() != null) {
//                                numberOfValidResponses++;
//                            }
//                            break;
//                        case "Texte":
//                            if (userResponse.getTextResponse() != null) {
//                                numberOfValidResponses++;
//                            }
//                            break;
//                        case "Commentaire": // Ajouté pour correspondre à getMyEvaluations
//                            if (userResponse.getCommentResponse() != null) {
//                                numberOfValidResponses++;
//                            }
//                            break;
//                        case "Réponse multiple":
//                            if (userResponse.getMultipleChoiceResponse() != null && !userResponse.getMultipleChoiceResponse().isEmpty()) {
//                                numberOfValidResponses++;
//                            }
//                            break;
//                        case "Réponse unique":
//                            if (userResponse.getSingleChoiceResponse() != null) {
//                                numberOfValidResponses++;
//                            }
//                            break;
//                        case "Evaluation": // Ajouté pour correspondre à getMyEvaluations
//                            if (userResponse.getSingleLevelChoiceResponse() != null) {
//                                numberOfValidResponses++;
//                            }
//                            break;
//                        default:
//                            log.warn("Type de réponse non géré lors du comptage pour l'utilisateur {}: {}", userId, userResponse.getResponseType());
//                            break;
//                    }
//                }
//
//                // Calculer la progression
//                if (totalQuestions > 0) {
//                    progress = (numberOfValidResponses * 100) / totalQuestions;
//                } else {
//                    progress = 0; // Ou 100 si aucune question signifie "terminé", mais 0 est cohérent avec getMyEvaluations
//                }
//
//                // Déterminer le statut basé sur la progression calculée
//                // (aligné avec la logique de getMyEvaluations)
//                if (progress == 100) {
//                    status = "Terminé"; // Utilisation de "Terminé" comme dans l'ancien getTeamEvaluationDetails
//                } else if (progress > 0) { // Couvre > 0 et < 100
//                    status = "En cours";
//                }
//                // Si progress == 0, le statut reste "En attente" (valeur par défaut)
//
//                // Récupérer les détails de l'utilisateur (nom, position) via authServiceClient
//                TeamEvaluationDetailsForUserDto userDetails = authServiceClient.getParticipant(userId);
//                if (userDetails == null) { // Bonne pratique: gérer le cas où userDetails pourrait être null
//                    log.warn("Détails non trouvés pour l'utilisateur ID: {}", userId);
//                    // Vous pourriez vouloir initialiser userDetails avec des valeurs par défaut ou sauter ce participant
//                    userDetails = TeamEvaluationDetailsForUserDto.builder()
//                            .name("Utilisateur Inconnu")
//                            .position("N/A")
//                            .build();
//                }
//
//
//                participantsDetails.add(TeamEvaluationDetailsForUserDto.builder()
//                        .id(userId)
//                        .name(userDetails.getName())
//                        .position(userDetails.getPosition())
//                        .progress(progress) // Progression calculée
//                        .status(status)     // Statut calculé
//                        .build());
//            }
//        }
//
//        // Calcul de l'état global et de la progression globale pour l'évaluation (inchangé)
//        String globalStatus = calculateGlobalStatusForTeam(allTeamResponsesForQuestionnaire);
//        Integer globalProgress = calculateGlobalProgressForTeam(allTeamResponsesForQuestionnaire);
//
//        TeamEvaluationDetailsDto detailsDto = TeamEvaluationDetailsDto.builder()
//                .id(questionnaire.getId())
//                .title(questionnaire.getTitle())
//                .status(globalStatus)
//                .creationDate(questionnaire.getCreationDate().toString())
//                .type(questionnaire.getType())
//                .participants(participantsDetails)
//                .progress(globalProgress)
//                .build();
//
//        return ResponseEntity.ok(detailsDto);
//    }


    /**/

    /**
     * Calcule la progression d'un utilisateur spécifique pour un questionnaire donné.
     * Basé sur la logique de getMyEvaluations.
     *
     * @param userId                        L'ID de l'utilisateur.
     * @param questionnaire                 Le questionnaire concerné.
     * @param userResponsesForQuestionnaire La liste des réponses de l'utilisateur pour ce questionnaire.
     * @return La progression en pourcentage (0-100).
     */
    public int calculateUserProgressForQuestionnaire(Long userId, Questionnaire questionnaire, List<UserResponse> userResponsesForQuestionnaire) {
        if (questionnaire == null || questionnaire.getQuestions() == null) {
            log.warn("Questionnaire est null ou n'a pas de questions pour le calcul de progression de l'utilisateur {}.", userId);
            return 0;
        }
        int totalQuestions = questionnaire.getQuestions().size();
        if (totalQuestions == 0) {
            return 0; // Ou 100 si aucune question signifie "terminé", mais 0 est plus prudent.
        }

        int numberOfValidResponses = 0;
        for (UserResponse userResponse : userResponsesForQuestionnaire) {
            // S'assurer que la réponse appartient bien à l'utilisateur et au questionnaire (normalement déjà filtré)
            if (!userResponse.getUserId().equals(userId) || !userResponse.getQuestionnaireId().equals(questionnaire.getId())) {
                continue;
            }
            switch (userResponse.getResponseType()) {
                case "Score":
                    if (userResponse.getScoreResponse() != null) numberOfValidResponses++;
                    break;
                case "Notation":
                    if (userResponse.getRatingResponse() != null) numberOfValidResponses++;
                    break;
                case "Texte":
                    if (userResponse.getTextResponse() != null) numberOfValidResponses++;
                    break;
                case "Commentaire":
                    if (userResponse.getCommentResponse() != null) numberOfValidResponses++;
                    break;
                case "Réponse multiple":
                    if (userResponse.getMultipleChoiceResponse() != null && !userResponse.getMultipleChoiceResponse().isEmpty())
                        numberOfValidResponses++;
                    break;
                case "Réponse unique":
                    if (userResponse.getSingleChoiceResponse() != null) numberOfValidResponses++;
                    break;
                case "Evaluation":
                    if (userResponse.getSingleLevelChoiceResponse() != null) numberOfValidResponses++;
                    break;
                default:
                    // Pas de log ici pour ne pas surcharger, sauf si c'est un type vraiment inattendu
                    break;
            }
        }

        return (numberOfValidResponses * 100) / totalQuestions;
    }

    /**
     * Calcule la progression globale pour une équipe sur un questionnaire.
     * C'est la moyenne des progressions individuelles des membres de l'équipe.
     * Chaque participant a une seule progression prise en compte.
     *
     * @param questionnaire                    Le questionnaire.
     * @param allTeamResponsesForQuestionnaire Toutes les réponses de l'équipe pour ce questionnaire.
     * @param teamMemberIds                    La liste des IDs des membres de l'équipe concernés.
     * @return La progression globale moyenne.
     */
    private Integer calculateGlobalProgressForTeam(
            Questionnaire questionnaire,
            List<UserResponse> allTeamResponsesForQuestionnaire,
            List<Long> teamMemberIds) {

        if (questionnaire == null || teamMemberIds == null || teamMemberIds.isEmpty()) {
            return 0;
        }

        List<Integer> individualProgressions = new ArrayList<>();
        for (Long userId : teamMemberIds) {
            // Filtrer les réponses pour l'utilisateur courant
            List<UserResponse> userSpecificResponses = allTeamResponsesForQuestionnaire.stream()
                    .filter(response -> response.getUserId().equals(userId))
                    .toList();

            int userProgress = calculateUserProgressForQuestionnaire(userId, questionnaire, userSpecificResponses);
            individualProgressions.add(userProgress);
        }

        if (individualProgressions.isEmpty()) {
            return 0;
        }

        double averageProgress = individualProgressions.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return (int) Math.round(averageProgress);
    }

    /**
     * Calcule le statut global pour une équipe sur un questionnaire.
     * Basé sur les statuts individuels dérivés de la progression.
     *
     * @param questionnaire                    Le questionnaire.
     * @param allTeamResponsesForQuestionnaire Toutes les réponses de l'équipe pour ce questionnaire.
     * @param teamMemberIds                    La liste des IDs des membres de l'équipe concernés.
     * @return Le statut global ("En attente", "En cours", "Terminé").
     */
    private String calculateGlobalStatusForTeam(
            Questionnaire questionnaire,
            List<UserResponse> allTeamResponsesForQuestionnaire,
            List<Long> teamMemberIds) {

        if (questionnaire == null || teamMemberIds == null || teamMemberIds.isEmpty()) {
            return "En attente";
        }

        List<String> individualStatuses = new ArrayList<>();
        for (Long userId : teamMemberIds) {
            // Filtrer les réponses pour l'utilisateur courant
            List<UserResponse> userSpecificResponses = allTeamResponsesForQuestionnaire.stream()
                    .filter(response -> response.getUserId().equals(userId))
                    .toList();

            int progress = calculateUserProgressForQuestionnaire(userId, questionnaire, userSpecificResponses);
            String userStatus = "En attente";
            if (progress == 100) {
                userStatus = "Terminé";
            } else if (progress > 0) {
                userStatus = "En cours";
            }
            individualStatuses.add(userStatus);
        }

        if (individualStatuses.isEmpty()) {
            return "En attente";
        }

        if (individualStatuses.stream().allMatch("Terminé"::equalsIgnoreCase)) {
            return "Terminé";
        }
        if (individualStatuses.stream().anyMatch("En cours"::equalsIgnoreCase)) {
            return "En cours";
        }
        // Si certains sont "Terminé" et d'autres "En attente" (mais aucun "En cours")
        // cela pourrait aussi être considéré comme "En cours" pour l'équipe.
        // La logique ci-dessous, qui est plus simple, considère ce cas comme "En attente"
        // s'il n'y a aucun "En cours". Vous pouvez ajuster si besoin.
        // Exemple: User A "Terminé", User B "En attente" -> Global "En attente"
        // Si vous préférez "En cours" dans ce cas:
        if (individualStatuses.stream().anyMatch("Terminé"::equalsIgnoreCase) &&
                individualStatuses.stream().anyMatch("En attente"::equalsIgnoreCase)) {
            // Optionnel: traiter un mélange de "Terminé" et "En attente" comme "En cours"
            // return "En cours";
        }

        return "En attente"; // Par défaut si pas tous terminés et aucun en cours
    }

    /*
     * @param questionnaire
     * @param allTeamResponsesForQuestionnaire
     * @param teamMemberIds
     * */
    private String calculateGlobalIsSentToManager(
            Questionnaire questionnaire,
            List<UserResponse> allTeamResponsesForQuestionnaire,
            List<Long> teamMemberIds) {

        // Vérification des paramètres
        if (questionnaire == null || teamMemberIds == null || teamMemberIds.isEmpty()) {
            return "Brouillon";
        }

        // Statut par défaut
        String globalStatus = "Terminée";  // On part du principe que tout est terminé

        // Pour chaque utilisateur de l'équipe
        for (Long userId : teamMemberIds) {
            // Filtrer les réponses pour l'utilisateur courant
            List<UserResponse> userSpecificResponses = allTeamResponsesForQuestionnaire.stream()
                    .filter(response -> response.getUserId().equals(userId))
                    .toList();

            // Si l'utilisateur n'a pas de réponses, on considère que c'est un brouillon
            if (userSpecificResponses.isEmpty()) {
                return "Brouillon";
            }

            // Vérifier si toutes les réponses de l'utilisateur sont envoyées
            boolean allSent = userSpecificResponses.stream()
                    .allMatch(response -> response.getIsSentToManager() != null && response.getIsSentToManager());

            // Si au moins une réponse n'est pas envoyée, tout est considéré comme brouillon
            if (!allSent) {
                return "Brouillon";
            }
        }

        // Si on arrive ici, c'est que toutes les réponses de tous les utilisateurs sont envoyées
        return globalStatus;
    }

    private String calculateGlobalIsSentToAdmin(
            Questionnaire questionnaire,
            List<UserResponse> allTeamResponsesForQuestionnaire,
            List<Long> teamMemberIds) {

        // Vérification des paramètres
        if (questionnaire == null || teamMemberIds == null || teamMemberIds.isEmpty()) {
            return "Brouillon";
        }

        // Statut par défaut
        String globalStatus = "Terminée";  // On part du principe que tout est terminé

        // Pour chaque utilisateur de l'équipe
        for (Long userId : teamMemberIds) {
            // Filtrer les réponses pour l'utilisateur courant
            List<UserResponse> userSpecificResponses = allTeamResponsesForQuestionnaire.stream()
                    .filter(response -> response.getUserId().equals(userId))
                    .toList();

            // Si l'utilisateur n'a pas de réponses, on considère que c'est un brouillon
            if (userSpecificResponses.isEmpty()) {
                return "Brouillon";
            }

            // Vérifier si toutes les réponses de l'utilisateur sont envoyées
            boolean allSent = userSpecificResponses.stream()
                    .allMatch(response -> response.getIsSentToAdmin() != null && response.getIsSentToAdmin());

            // Si au moins une réponse n'est pas envoyée, tout est considéré comme brouillon
            if (!allSent) {
                return "Brouillon";
            }
        }

        // Si on arrive ici, c'est que toutes les réponses de tous les utilisateurs sont envoyées
        return globalStatus;
    }

    private String calculateIndividualIsSentToManager(
            Questionnaire questionnaire,
            List<UserResponse> userResponses,
            Long userId) {

        // Vérification des paramètres
        if (questionnaire == null || userResponses == null || userId == null) {
            return "Brouillon";
        }

        // Filtrer les réponses pour l'utilisateur spécifié
        List<UserResponse> userSpecificResponses = userResponses.stream()
                .filter(response -> response.getUserId().equals(userId))
                .toList();

        // Si l'utilisateur n'a pas de réponses, on considère que c'est un brouillon
        if (userSpecificResponses.isEmpty()) {
            return "Brouillon";
        }

        // Vérifier si toutes les réponses de l'utilisateur sont envoyées
        boolean allSent = userSpecificResponses.stream()
                .allMatch(response -> response.getIsSentToManager() != null && response.getIsSentToManager());

        // Si toutes les réponses sont envoyées, c'est terminé, sinon c'est un brouillon
        return allSent ? "Terminée" : "Brouillon";
    }

    private String calculateIndividualIsSentToAdmin(
            Questionnaire questionnaire,
            List<UserResponse> userResponses,
            Long userId) {

        // Vérification des paramètres
        if (questionnaire == null || userResponses == null || userId == null) {
            return "Brouillon";
        }

        // Filtrer les réponses pour l'utilisateur spécifié
        List<UserResponse> userSpecificResponses = userResponses.stream()
                .filter(response -> response.getUserId().equals(userId))
                .toList();

        // Si l'utilisateur n'a pas de réponses, on considère que c'est un brouillon
        if (userSpecificResponses.isEmpty()) {
            return "Brouillon";
        }

        // Vérifier si toutes les réponses de l'utilisateur sont envoyées
        boolean allSent = userSpecificResponses.stream()
                .allMatch(response -> response.getIsSentToAdmin() != null && response.getIsSentToAdmin());

        // Si toutes les réponses sont envoyées, c'est terminé, sinon c'est un brouillon
        return allSent ? "Terminée" : "Brouillon";
    }

    // 3. Modification de getTeamEvaluations
    @Override
    public ResponseEntity<List<GetTeamEvaluationsDto>> getTeamEvaluations(Long managerId) {
        List<Long> myTeamIds = authServiceClient.getMyTeam(managerId);
        List<GetTeamEvaluationsDto> teamEvaluationDtos = new ArrayList<>();

        if (myTeamIds != null && !myTeamIds.isEmpty()) {
            // On récupère toutes les campagnes publiées où au moins un membre de l'équipe est participant.
            List<CampaignEvaluation> campaignEvaluations = campaignEvaluationRepository.findByAnyParticipantIdInAndStatus(myTeamIds, "Publiée");

            for (CampaignEvaluation campaignEvaluation : campaignEvaluations) {
                for (Questionnaire questionnaire : campaignEvaluation.getQuestionnaires()) {
                    // Récupérer les réponses des membres de l'équipe pour CE QUESTIONNAIRE
                    // Il est important de ne récupérer que les réponses des membres de l'équipe concernés par CE questionnaire
                    // La logique de `campaignEvaluation.getParticipantIds()` pourrait être utilisée pour affiner `myTeamIds`
                    // si `myTeamIds` est plus large que les participants réels à la campagne/questionnaire.
                    // Pour l'instant, on suppose que `myTeamIds` sont tous potentiellement concernés.
                    List<Long> myTeamIdsForThisQuestionnaire = myTeamIds.stream()
                            .filter(campaignEvaluation.getParticipantIds()::contains)
                            .collect(Collectors.toList()); // Collecte en List
                    List<UserResponse> teamResponsesForQuestionnaire =
                            userResponseRepository.findByQuestionnaireIdAndUserIdIn(questionnaire.getId(), myTeamIds);

                    // Utilisation des nouvelles méthodes utilitaires
                    Integer globalProgress = calculateGlobalProgressForTeam(questionnaire, teamResponsesForQuestionnaire, myTeamIdsForThisQuestionnaire);
//                    String globalStatus = calculateGlobalStatusForTeam(questionnaire, teamResponsesForQuestionnaire, myTeamIdsForThisQuestionnaire);

                    String globalIsSentToManager = calculateGlobalIsSentToAdmin(questionnaire, teamResponsesForQuestionnaire, myTeamIdsForThisQuestionnaire);

                    GetTeamEvaluationsDto dto = GetTeamEvaluationsDto.builder()
                            .id(questionnaire.getId())
                            .title(questionnaire.getTitle())
                            .status(globalIsSentToManager) // Statut global calculé
                            .creationDate(questionnaire.getCreationDate().toString()) // Assurez-vous que getCreationDate() n'est pas null
                            .type(questionnaire.getType())
                            .participantIds(myTeamIdsForThisQuestionnaire)
                            .participants(myTeamIdsForThisQuestionnaire.size()) // Nombre de membres de l'équipe potentiellement concernés
                            .progress(globalProgress) // Progression globale calculée
                            .build();
                    teamEvaluationDtos.add(dto);
                }
            }
        }
        return ResponseEntity.ok(teamEvaluationDtos);
    }

    // 4. Modification de getTeamEvaluationDetails
    @Override
    public ResponseEntity<?> getTeamEvaluationDetails(UUID questionnaireId, Long managerId) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new RuntimeException("Questionnaire non trouvé avec ID: " + questionnaireId));

        List<QuestionDto> questionDtos = new ArrayList<>();
        questionnaire.getQuestions().forEach(question -> {
            questionDtos.add(EvaluationUtilMethods.mapToQuestionDto(question));
        });

        List<Long> myTeamIds = authServiceClient.getMyTeam(managerId);
        // log.debug("My team IDs : {}.", myTeamIds); // Utilisez log.info ou log.debug

        List<CampaignEvaluation> campaignEvaluations = campaignEvaluationRepository.findByAnyParticipantIdInAndStatus(myTeamIds, "Publiée");
        List<Long> teamParticipantIds = campaignEvaluations.get(0).getParticipantIds().stream().filter(myTeamIds::contains).toList();

        List<TeamEvaluationDetailsForUserDto> participantsDetails = new ArrayList<>();
        // Récupérer toutes les réponses de l'équipe pour ce questionnaire spécifique en une seule fois
        List<UserResponse> allTeamResponsesForQuestionnaire =
                userResponseRepository.findByQuestionnaireIdAndUserIdIn(questionnaireId, teamParticipantIds);

        if (!teamParticipantIds.isEmpty()) {
            log.info("Traitement des détails d'évaluation pour le questionnaire {} pour les membres de l'équipe.", questionnaireId);

            for (Long userId : teamParticipantIds) {
                // Filtrer les réponses pour l'utilisateur courant à partir de la liste déjà chargée
                List<UserResponse> userResponsesForThisUser = allTeamResponsesForQuestionnaire.stream()
                        .filter(response -> response.getUserId().equals(userId))
                        .toList();

                // Calcul de la progression individuelle en utilisant la méthode utilitaire
                int progress = calculateUserProgressForQuestionnaire(userId, questionnaire, userResponsesForThisUser);

                // Déterminer le statut individuel basé sur la progression calculée
                String status = calculateIndividualIsSentToAdmin(questionnaire, userResponsesForThisUser, userId);

                TeamEvaluationDetailsForUserDto userDetailsFromAuth = authServiceClient.getParticipant(userId);
                String name = "Utilisateur Inconnu";
                String position = "N/A";
                String groupe = "N/A";
                if (userDetailsFromAuth != null) {
                    name = userDetailsFromAuth.getName();
                    position = userDetailsFromAuth.getPosition();
                    groupe = userDetailsFromAuth.getGroupe();
                } else {
                    log.warn("Détails non trouvés pour l'utilisateur ID: {}. Utilisation de valeurs par défaut.", userId);
                }

                Boolean isSentToManger = false; // Valeur par défaut si aucune réponse n'est trouvée
                Boolean isSentToAdmin = false; // Valeur par défaut si aucune réponse n'est trouvée
                if (!userResponsesForThisUser.isEmpty()) {
                    // Si la liste n'est PAS vide, on peut accéder au premier élément
                    isSentToManger = userResponsesForThisUser.get(0).getIsSentToManager();
                    isSentToAdmin = userResponsesForThisUser.get(0).getIsSentToAdmin();
                } else {
                    // Gérer le cas où la liste est vide.
                    // Ici, isSent reste null comme initialisé.
                    // Tu peux ajouter un log ou définir une autre valeur par défaut si besoin.
                    log.info("Aucune réponse trouvée pour l'utilisateur ID: {} dans allTeamResponsesForQuestionnaire. isSent sera null.", userId);
                }

                participantsDetails.add(TeamEvaluationDetailsForUserDto.builder()
                        .id(userId)
                        .name(name)
                        .position(position)
                        .groupe(groupe)
                        .progress(progress)
                        .status(status)
                        .isSentToManager(isSentToManger)
                        .isSentToAdmin(isSentToAdmin)
                        .build());
            }
        }

        // Calcul de la progression globale et du statut global en utilisant les méthodes utilitaires
        Integer globalProgress = calculateGlobalProgressForTeam(questionnaire, allTeamResponsesForQuestionnaire, teamParticipantIds);
        String globalStatus = calculateGlobalStatusForTeam(questionnaire, allTeamResponsesForQuestionnaire, teamParticipantIds);

        TeamEvaluationDetailsDto detailsDto = TeamEvaluationDetailsDto.builder()
                .id(questionnaire.getId())
                .title(questionnaire.getTitle())
                .status(globalStatus) // Statut global calculé
                .creationDate(questionnaire.getCreationDate().toString()) // Assurez-vous que getCreationDate() n'est pas null
                .type(questionnaire.getType())
                .participants(participantsDetails) // Liste des détails des participants
                .progress(globalProgress) // Progression globale calculée
                .questions(questionDtos)
                .build();

        return ResponseEntity.ok(detailsDto);
    }

//    @Transactional
//    @Override
//    public ResponseEntity<?> sendEvaluationToAdmin(UUID id, SendEvaluationToAdminDto sendEvaluationToAdminDto) {
//        List<UserResponse> userResponses = userResponseRepository.findByQuestionnaireIdAndUserIdIn(id, sendEvaluationToAdminDto.getParticipantIds());
//        userResponses.forEach(userResponse -> {
//            userResponse.setIsSentToAdmin(true);
//        });
//        userResponseRepository.saveAll(userResponses);
//        return ResponseEntity.ok().build();
//    }

    @Transactional
    public ResponseEntity<?> sendEvaluationToAdmin(UUID questionnaireId, SendEvaluationToAdminDto sendEvaluationToAdminDto) {
        // Étape 1: Mettre à jour les UserResponses (logique existante)
        List<UserResponse> userResponsesToUpdate = userResponseRepository.findByQuestionnaireIdAndUserIdIn(
                questionnaireId, sendEvaluationToAdminDto.getParticipantIds()
        );

        if (userResponsesToUpdate.isEmpty() && !sendEvaluationToAdminDto.getParticipantIds().isEmpty()) {
            // Gérer le cas où aucun UserResponse n'est trouvé pour les participants spécifiés.
            // Vous pourriez retourner une erreur ou simplement un log.
            System.out.println("Aucune réponse utilisateur trouvée pour le questionnaire " + questionnaireId +
                    " et les participants fournis.");
            // Optionnel: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucune réponse utilisateur trouvée.");
        }

        userResponsesToUpdate.forEach(userResponse -> {
            userResponse.setIsSentToAdmin(true);
            userResponse.setLastModifiedDate(LocalDate.now());
        });
        userResponseRepository.saveAll(userResponsesToUpdate);

        // Étape 2: Créer conditionnellement un "Besoin" (Need)
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElse(null);

        if (questionnaire == null) {
            System.err.println("Questionnaire non trouvé avec l'ID: " + questionnaireId + ". La création de Besoin est annulée.");
            // Vous pouvez choisir de retourner une réponse d'erreur ici si le questionnaire est crucial.
            // Pour l'instant, on retourne OK car la première partie (mise à jour de isSentToAdmin) a réussi.
            return ResponseEntity.ok().body("Évaluations envoyées à l'admin. Questionnaire non trouvé pour la création de Besoin.");
        }

        if ("Récensement des besoins de formation".equals(questionnaire.getType())) {
            // Trouver la définition de la question "Thème" dans le questionnaire
            Question themeQuestionDefinition = questionnaire.getQuestions().stream()
                    .filter(q -> "Thème".equals(q.getText())) // Attention: sensible à la casse et au texte exact
                    .findFirst()
                    .orElse(null);

            if (themeQuestionDefinition == null) {
                System.out.println("Attention: La question 'Thème' n'a pas été trouvée dans le questionnaire '" +
                        questionnaire.getTitle() + "'. Impossible de créer des Besoins.");
            } else {
                UUID themeQuestionId = themeQuestionDefinition.getId();
                String themeQuestionType = themeQuestionDefinition.getType(); // Type de la question "Thème"

                // Regrouper toutes les réponses par userId pour faciliter la recherche
                // `userResponsesToUpdate` contient déjà les réponses pour les utilisateurs et le questionnaire concernés.
                Map<Long, List<UserResponse>> responsesByUserId = userResponsesToUpdate.stream()
                        .collect(Collectors.groupingBy(UserResponse::getUserId));

                for (Long participantId : sendEvaluationToAdminDto.getParticipantIds()) {
                    List<UserResponse> participantResponses = responsesByUserId.get(participantId);
                    if (participantResponses == null || participantResponses.isEmpty()) {
                        System.out.println("Attention: Aucune réponse trouvée pour le participant " + participantId +
                                " après le premier fetch. Création de Besoin annulée pour ce participant.");
                        continue;
                    }

                    UserResponse themeUserResponse = participantResponses.stream()
                            .filter(ur -> ur.getQuestionId().equals(themeQuestionId))
                            .findFirst()
                            .orElse(null);

                    String themeValue = null;
                    if (themeUserResponse != null) {
                        // Extraire la valeur de la réponse en fonction du type de la question "Thème"
                        switch (themeQuestionType) {
                            case "Texte": // Adaptez ces types à ceux définis dans votre application
//                            case "Text":
                                themeValue = themeUserResponse.getTextResponse();
                                break;
                            case "Réponse unique": // Type générique pour choix unique
                                // case "SINGLE_CHOICE_RADIO": // Exemple plus spécifique
                                // case "SINGLE_CHOICE_DROPDOWN": // Exemple plus spécifique
                                themeValue = themeUserResponse.getSingleChoiceResponse();
                                break;
                            case "Réponse multiple": // Type générique pour choix multiples
                                // case "MULTIPLE_CHOICE_CHECKBOX": // Exemple plus spécifique
                                if (themeUserResponse.getMultipleChoiceResponse() != null && !themeUserResponse.getMultipleChoiceResponse().isEmpty()) {
                                    themeValue = String.join(", ", themeUserResponse.getMultipleChoiceResponse());
                                }
                                break;
                            default:
                                System.out.println("Attention: Type de question non géré ('" + themeQuestionType +
                                        "') pour la question 'Thème'. Tentative d'extraction depuis les champs communs.");
                                // Tentative de secours (peut être affinée ou supprimée)
                                if (themeUserResponse.getTextResponse() != null && !themeUserResponse.getTextResponse().isEmpty()) {
                                    themeValue = themeUserResponse.getTextResponse();
                                } else if (themeUserResponse.getSingleChoiceResponse() != null && !themeUserResponse.getSingleChoiceResponse().isEmpty()) {
                                    themeValue = themeUserResponse.getSingleChoiceResponse();
                                }
                                break;
                        }
                        if (themeValue != null) themeValue = themeValue.trim();
                    }

                    if (themeValue == null || themeValue.isEmpty()) {
                        System.out.println("Attention: Réponse au 'Thème' non trouvée ou vide pour l'utilisateur " + participantId +
                                " et la question " + themeQuestionId + ". Création de Besoin annulée.");
                        continue;
                    }

                    // Récupérer le nom du demandeur (participant)
                    // Vous aurez besoin d'un service (ex: UserService) pour cela.
                    // String requesterName = userService.getUserNameById(participantId);
                    String requesterName = "Nom du demandeur pour ID " + participantId; // Placeholder

                    // Récupérer l'ID de l'approbateur (manager du participant)
                    // Vous aurez besoin d'un service (ex: UserService) pour cela.
                    // Long approverId = userService.getManagerIdForUser(participantId);
                    Long approverId = null;
                    UserDto userDto = authServiceClient.getUserById(participantId);
                    if (userDto != null) {
                        approverId = userDto.getManagerId();
                        requesterName = userDto.getName();
                    }

                    Long companyId = questionnaire.getCompanyId(); // Ou themeUserResponse.getCompanyId()

                    Need need = Need.builder()
                            .requesterId(participantId)
                            .requesterName(requesterName) // À implémenter: récupérer le nom de l'utilisateur
                            .approverId(approverId)       // À implémenter: récupérer l'ID du manager
                            .source(NeedSource.Evaluation)
                            .creationDate(DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())) // Format "YYYY-MM-DD"
                            .type("Evaluation") // Comme spécifié
                            .theme(themeValue)
                            .questionnaire(questionnaire.getTitle())
                            .companyId(companyId)
                            // Initialiser les autres champs requis de "Need"
                            .status(NeedStatusEnums.Brouillon)
                            .build();


                    // TODO : dé commenter cette section pour ajouter un groupe par défaut au besoin issu de l'évaluation
                    Groupe groupe = Groupe.builder()
                            .need(need) // Association du groupe au besoin
                            .companyId(companyId) // Récupération de l'ID de l'entreprise
                            .name("Groupe 1")
                            .status(GroupeStatusEnums.Brouillon)
                            .build();

                    need.setGroupes(List.of(groupe));

                    needRepository.save(need);
                    System.out.println("Besoin (Need) créé pour l'utilisateur " + participantId + " avec le thème: " + themeValue);
                }
            }
        }

        return ResponseEntity.ok().body("Opération terminée. Évaluations envoyées et Besoins créés si applicable.");
    }

    /**/
}