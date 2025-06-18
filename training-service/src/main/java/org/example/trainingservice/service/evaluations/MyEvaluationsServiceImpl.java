package org.example.trainingservice.service.evaluations;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.client.users.AuthServiceClient;
import org.example.trainingservice.dto.evaluation.*;
import org.example.trainingservice.entity.Need;
import org.example.trainingservice.entity.campaign.CampaignEvaluation;
import org.example.trainingservice.entity.campaign.Question;
import org.example.trainingservice.entity.campaign.Questionnaire;
import org.example.trainingservice.entity.campaign.UserResponse;
import org.example.trainingservice.enums.NeedSource;
import org.example.trainingservice.enums.NeedStatusEnums;
import org.example.trainingservice.repository.NeedRepository;
import org.example.trainingservice.repository.evaluation.CampaignEvaluationRepository;
import org.example.trainingservice.repository.evaluation.QuestionRepository;
import org.example.trainingservice.repository.evaluation.QuestionnaireRepository;
import org.example.trainingservice.repository.evaluation.UserResponseRepository;
import org.example.trainingservice.utils.EvaluationUtilMethods;
import org.example.trainingservice.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class MyEvaluationsServiceImpl implements MyEvaluationsService {
    private final QuestionnaireRepository questionnaireRepository;
    private final CampaignEvaluationRepository campaignEvaluationRepository;
    private final UserResponseRepository userResponseRepository;
    private final QuestionRepository questionRepository;
    private final AuthServiceClient authServiceClient;
    private final NeedRepository needRepository;

    public MyEvaluationsServiceImpl(QuestionnaireRepository questionnaireRepository, CampaignEvaluationRepository campaignEvaluationRepository, UserResponseRepository userResponseRepository, QuestionRepository questionRepository, AuthServiceClient authServiceClient, NeedRepository needRepository) {
        this.questionnaireRepository = questionnaireRepository;
        this.campaignEvaluationRepository = campaignEvaluationRepository;
        this.userResponseRepository = userResponseRepository;
        this.questionRepository = questionRepository;
        this.authServiceClient = authServiceClient;
        this.needRepository = needRepository;
    }

    @Override
    public ResponseEntity<?> getMyEvaluations(Long userId) {
        // liste des campagnes auxquelles appartient l'utilisateur
        log.info("Fetching campaign evaluations for user with ID {}.", userId);
        List<CampaignEvaluation> campaignEvaluations = campaignEvaluationRepository.findByParticipantIdsContainsAndStatus(userId, "Publiée");

        // liste des questionnaires associés aux campagnes
        log.info("Fetching questionnaires for campaign evaluations.");
        List<Questionnaire> questionnaires = new ArrayList<>();
        campaignEvaluations.forEach(campaignEvaluation -> {
            questionnaires.addAll(campaignEvaluation.getQuestionnaires());
        });

        // Liste des evaluations du user
        log.info("Fetching evaluations for user with ID {}.", userId);
        List<MyEvaluationsDto> myEvaluationsDtos = new ArrayList<>();

        questionnaires.forEach(questionnaire -> {
            // Récupérer toutes les réponses de l'utilisateur pour ce questionnaire
            List<UserResponse> userResponsesForQuestionnaireAndUser =
                    userResponseRepository.findByUserIdAndQuestionnaireId(userId, questionnaire.getId());

            // Récupérer le nombre total de questions du questionnaire
            int totalQuestions = questionnaire.getQuestions().size();
            int numberOfValidResponses = 0;

            // Compter le nombre de réponses valides
            for (UserResponse userResponse : userResponsesForQuestionnaireAndUser) {
                switch (userResponse.getResponseType()) {
                    case "Score":
                        if (userResponse.getScoreResponse() != null) {
                            numberOfValidResponses++;
                        }
                        break;
                    case "Notation":
                        if (userResponse.getRatingResponse() != null) {
                            numberOfValidResponses++;
                        }
                        break;
                    case "Texte":
                        if (userResponse.getTextResponse() != null) {
                            numberOfValidResponses++;
                        }
                        break;
                    case "Commentaire":
                        if (userResponse.getCommentResponse() != null) {
                            numberOfValidResponses++;
                        }
                        break;
                    case "Réponse multiple":
                        if (userResponse.getMultipleChoiceResponse() != null && !userResponse.getMultipleChoiceResponse().isEmpty()) {
                            numberOfValidResponses++;
                        }
                        break;
                    case "Réponse unique":
                        if (userResponse.getSingleChoiceResponse() != null) {
                            numberOfValidResponses++;
                        }
                        break;
                    case "Evaluation":
                        if (userResponse.getSingleLevelChoiceResponse() != null) {
                            numberOfValidResponses++;
                        }
                        // Nous traiterons ce cas plus tard
                        break;
                    default:
                        log.warn("Type de réponse non géré : {}", userResponse.getResponseType());
                        break;
                }
            }

            // Calculer la progression
            int progression = 0;
            if (totalQuestions > 0) {
                progression = (numberOfValidResponses * 100) / totalQuestions;
            }

            log.error("Progression: {}.", progression);

            // Déterminer le statut
            String status = "En attente";
            LocalDate startDate = null;
            if (progression > 0 && progression < 100) {
                status = "En cours";
                // On prend la date de la première réponse (valide ou non) comme date de début
                if (!userResponsesForQuestionnaireAndUser.isEmpty()) {
                    startDate = userResponsesForQuestionnaireAndUser.get(0).getStartDate();
                }
            } else if (progression == 100) {
                status = "Terminée";
                log.error("Status {}.", status);
                if (!userResponsesForQuestionnaireAndUser.isEmpty()) {
                    startDate = userResponsesForQuestionnaireAndUser.get(0).getStartDate();
                }
            }

            // Nouvelle liste de questions pour chaque questionnaire
            List<QuestionDto> questionDtos = new ArrayList<>();
            questionnaire.getQuestions().forEach(question -> {
                questionDtos.add(EvaluationUtilMethods.mapToQuestionDto(question));
            });

            MyEvaluationsDto myEvaluationsDto = MyEvaluationsDto.builder()
                    .id(questionnaire.getId())
                    .title(questionnaire.getTitle())
                    .type(questionnaire.getType())
                    .progress(progression)
                    .status(status)
                    .questions(questionDtos)
                    .startDate(startDate != null ? startDate.toString() : "Pas encore")
                    .build();

            myEvaluationsDtos.add(myEvaluationsDto);
        });
        log.info("Returning {} evaluations for user with ID {}.", myEvaluationsDtos.size(), userId);
        return ResponseEntity.ok().body(myEvaluationsDtos);
    }

    @Override
    public ResponseEntity<?> addUserResponse(UUID questionnaireId, List<AddUserResponseDto> addUserResponseDtos) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId).orElseThrow(RuntimeException::new);
        List<UserResponse> responsesToSave = new ArrayList<>();
        int numberOfQuestions = questionnaire.getQuestions().size();
        int numberOfValidResponses = 0;

        log.error("Questionnaire {} : {} questions.", questionnaireId, numberOfQuestions);
        log.error("Add User Responses Dtos : {}.", addUserResponseDtos.size());

        for (AddUserResponseDto addUserResponseDto : addUserResponseDtos) {
            boolean hasValidResponse = false;
            switch (addUserResponseDto.getResponseType()) {
                case "Score":
                    if (addUserResponseDto.getScoreResponse() != null) {
                        hasValidResponse = true;
                    }
                    break;
                case "Notation":
                    if (addUserResponseDto.getRatingResponse() != null) {
                        hasValidResponse = true;
                    }
                    break;
                case "Texte":
                    if (addUserResponseDto.getTextResponse() != null) {
                        hasValidResponse = true;
                    }
                    break;
                case "Commentaire":
                    if (addUserResponseDto.getCommentResponse() != null) {
                        hasValidResponse = true;
                    }
                    break;
                case "Réponse multiple":
                    if (addUserResponseDto.getMultipleChoiceResponse() != null && !addUserResponseDto.getMultipleChoiceResponse().isEmpty()) {
                        hasValidResponse = true;
                    }
                    break;
                case "Réponse unique":
                    if (addUserResponseDto.getSingleChoiceResponse() != null) {
                        hasValidResponse = true;
                    }
                    break;
                case "Evaluation":
                    // Nous traiterons ce cas plus tard
                    break;
                default:
                    break;
            }

            if (hasValidResponse) {
                numberOfValidResponses++;
            }


            // Vérifier si une réponse existe déjà pour cet utilisateur, ce questionnaire et cette question
            UserResponse existingResponse = userResponseRepository.findByUserIdAndQuestionnaireIdAndQuestionId(
                    addUserResponseDto.getUserId(), questionnaireId, addUserResponseDto.getQuestionId()
            ).orElse(null);

            int progression = (numberOfQuestions > 0) ? (numberOfValidResponses * 100) / numberOfQuestions : 0;

            UserResponse.UserResponseBuilder builder;
            if (existingResponse != null) {
                // Si une réponse existe, on la met à jour
                builder = existingResponse.toBuilder()
                        .lastModifiedDate(LocalDate.now()) // Mettre à jour la date de modification
                        .progression(progression);
            } else {
                // Si aucune réponse n'existe, on en crée une nouvelle
                builder = UserResponse.builder()
                        .userId(addUserResponseDto.getUserId())
                        .isSentToManager(false)
                        .isSentToAdmin(false)
                        .companyId(SecurityUtils.getCurrentCompanyId())
                        .questionnaireId(questionnaireId)
                        .questionId(addUserResponseDto.getQuestionId())
                        .startDate(LocalDate.now())
                        .progression(progression)
                        .responseType(addUserResponseDto.getResponseType());
            }

            switch (addUserResponseDto.getResponseType()) {
                case "Score":
                    builder.scoreResponse(addUserResponseDto.getScoreResponse());
                    break;
                case "Notation":
                    builder.ratingResponse(addUserResponseDto.getRatingResponse());
                    break;
                case "Texte":
                    builder.textResponse(addUserResponseDto.getTextResponse());
                    break;
                case "Commentaire":
                    builder.commentResponse(addUserResponseDto.getCommentResponse());
                    break;
                case "Réponse multiple":
                    builder.multipleChoiceResponse(addUserResponseDto.getMultipleChoiceResponse());
                    break;
                case "Réponse unique":
                    builder.singleChoiceResponse(addUserResponseDto.getSingleChoiceResponse());
                    break;
                case "Evaluation":

                    // Ici, nous ajouterons la logique pour l'évaluation plus tard
                    break;
                default:
                    break;
            }

            if (progression == 100) {
                builder.status("Terminée");
            } else if (progression < 100 && progression > 0) {
                builder.status("En cours");
            } else {
                builder.status(existingResponse != null ? existingResponse.getStatus() : null); // Conserver l'ancien statut si existant
            }

            responsesToSave.add(builder.build());
        }

        userResponseRepository.saveAll(responsesToSave);

        return ResponseEntity.ok("Réponses utilisateur enregistrées avec succès.");
    }

    @Override
    public ResponseEntity<?> getAllUserResponses(Long userId, UUID questionnaireId) {
        List<GetUserResponsesDto> getUserResponsesDtos = new ArrayList<>();
        List<UserResponse> userResponses = userResponseRepository.findByUserIdAndQuestionnaireId(userId, questionnaireId);
        userResponses.forEach(userResponse -> {
            getUserResponsesDtos.add(EvaluationUtilMethods.mapToGetUserResponsesDto(userResponse));
        });
        return ResponseEntity.ok().body(getUserResponsesDtos);
    }

//    @Override
//    public ResponseEntity<?> sendEvaluation(UUID id, SendEvaluationDto sendEvaluationDto) {
//        // Récupérer les réponses de l'utilisateur
//        List<UserResponse> userResponses = userResponseRepository.findByUserIdAndQuestionnaireId(
//                sendEvaluationDto.getUserId(),
//                id
//        );
//
//        if (userResponses.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        // Récupérer les informations de l'utilisateur via Feign
//        UserDto user = authServiceClient.getUserById(sendEvaluationDto.getUserId());
//
//        // Vérifier le rôle et mettre à jour les champs correspondants
//        userResponses.forEach(userResponse -> {
//            if ("Collaborateur".equals(user.getRole())) {
//                userResponse.setIsSentToManager(true);
//            } else if ("Manager".equals(user.getRole())) {
//                userResponse.setIsSentToAdmin(true);
//            }
//            // Vous pourriez ajouter un else pour gérer d'autres rôles si nécessaire
//        });
//
//        // Sauvegarder les modifications
//        userResponseRepository.saveAll(userResponses);
//
//        return ResponseEntity.ok().build();
//    }

    @Override
    public ResponseEntity<?> sendEvaluation(UUID questionnaireUuid, SendEvaluationDto sendEvaluationDto) {
        Long userId = sendEvaluationDto.getUserId();

        // Étape 1: Récupérer les réponses de l'utilisateur pour le questionnaire donné
        List<UserResponse> userResponses = userResponseRepository.findByUserIdAndQuestionnaireId(
                userId,
                questionnaireUuid
        );

        if (userResponses.isEmpty()) {
            // log.warn("Aucune réponse utilisateur trouvée pour l'utilisateur {} et le questionnaire {}", userId, questionnaireUuid);
            System.out.println("Aucune réponse utilisateur trouvée pour l'utilisateur " + userId +
                    " et le questionnaire " + questionnaireUuid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucune réponse utilisateur trouvée pour cet utilisateur et ce questionnaire.");
        }

        // Étape 2: Récupérer les informations de l'utilisateur (notamment son rôle)
        UserDto userDto = authServiceClient.getUserById(userId);
        if (userDto == null) {
            // log.error("Utilisateur non trouvé avec l'ID: {}. Impossible de traiter l'envoi de l'évaluation.", userId);
            System.err.println("Utilisateur non trouvé avec l'ID: " + userId + ". Impossible de traiter l'envoi de l'évaluation.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé.");
        }
        String userRole = userDto.getRole();

        // Étape 3: Mettre à jour les UserResponses en fonction du rôle
        boolean requiresSave = false;
        final LocalDate nowDate = LocalDate.now(); // Pour la cohérence de la date de modification

        if ("Collaborateur".equals(userRole)) {
            for (UserResponse ur : userResponses) {
                if (!Boolean.TRUE.equals(ur.getIsSentToManager())) {
                    ur.setIsSentToManager(true);
                    ur.setLastModifiedDate(nowDate);
                    requiresSave = true;
                }
            }
        } else if ("Manager".equals(userRole)) {
            for (UserResponse ur : userResponses) {
                if (!Boolean.TRUE.equals(ur.getIsSentToAdmin())) {
                    ur.setIsSentToAdmin(true);
                    ur.setLastModifiedDate(nowDate);
                    requiresSave = true;
                }
            }
        } else {
            // log.warn("Rôle utilisateur '{}' non géré pour l'utilisateur {}. Aucune action d'envoi spécifique effectuée.", userRole, userId);
            System.out.println("Rôle utilisateur '" + userRole + "' non géré pour l'utilisateur " + userId + ". Aucune action d'envoi spécifique effectuée.");
            // Selon votre logique, vous pourriez retourner une erreur ici ou simplement ne rien faire de plus.
        }

        if (requiresSave) {
            userResponseRepository.saveAll(userResponses);
            // log.info("UserResponses mises à jour pour l'utilisateur {} et le questionnaire {}", userId, questionnaireUuid);
            System.out.println("UserResponses mises à jour pour l'utilisateur " + userId + " et le questionnaire " + questionnaireUuid);
        }

        // Étape 4: Si l'utilisateur est "Manager", tenter de créer un "Besoin" (Need)
        String operationStatusMessage = "Évaluation envoyée.";

        if ("Manager".equals(userRole)) {
            // log.info("L'utilisateur {} est un Manager. Tentative de création de Besoin pour le questionnaire {}.", userId, questionnaireUuid);
            System.out.println("L'utilisateur " + userId + " est un Manager. Tentative de création de Besoin pour le questionnaire " + questionnaireUuid + ".");

            Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findById(questionnaireUuid);

            if (questionnaireOpt.isEmpty()) {
                // log.warn("Questionnaire non trouvé avec l'ID: {}. La création de Besoin est annulée pour l'utilisateur {}.", questionnaireUuid, userId);
                System.err.println("Questionnaire non trouvé avec l'ID: " + questionnaireUuid + ". La création de Besoin est annulée pour l'utilisateur " + userId + ".");
                // Ne pas retourner une erreur ici, car la mise à jour de UserResponse a pu réussir.
                // Le message de retour final indiquera le succès partiel.
                operationStatusMessage += " Cependant, le questionnaire n'a pas été trouvé pour la création de Besoin.";
            } else {
                Questionnaire questionnaire = questionnaireOpt.get();
                if ("Récensement des besoins de formation".equals(questionnaire.getType())) {
                    // log.info("Le questionnaire '{}' est de type 'Récensement des besoins de formation'. Recherche de la question 'Thème'.", questionnaire.getTitle());
                    System.out.println("Le questionnaire '" + questionnaire.getTitle() + "' est de type 'Récensement des besoins de formation'. Recherche de la question 'Thème'.");

                    Question themeQuestionDefinition = questionnaire.getQuestions().stream()
                            .filter(q -> "Thème".equals(q.getText())) // Sensible à la casse et au texte exact
                            .findFirst()
                            .orElse(null);

                    if (themeQuestionDefinition == null) {
                        // log.warn("Question 'Thème' non trouvée dans le questionnaire '{}' (ID: {}). Création de Besoin annulée pour l'utilisateur {}.", questionnaire.getTitle(), questionnaireUuid, userId);
                        System.out.println("Attention: La question 'Thème' n'a pas été trouvée dans le questionnaire '" +
                                questionnaire.getTitle() + "'. Impossible de créer des Besoins pour l'utilisateur " + userId);
                        operationStatusMessage += " La question 'Thème' est introuvable dans le questionnaire, le Besoin n'a pas été créé.";
                    } else {
                        UUID themeQuestionId = themeQuestionDefinition.getId();
                        String themeQuestionType = themeQuestionDefinition.getType();
                        // log.debug("Question 'Thème' trouvée (ID: {}, Type: {}). Recherche de la réponse de l'utilisateur {}.", themeQuestionId, themeQuestionType, userId);
                        System.out.println("Question 'Thème' trouvée (ID: " + themeQuestionId + ", Type: " + themeQuestionType + "). Recherche de la réponse de l'utilisateur " + userId + ".");


                        // Les `userResponses` contiennent toutes les réponses de l'utilisateur pour ce questionnaire.
                        // Filtrons pour trouver la réponse à la question "Thème".
                        UserResponse themeUserResponse = userResponses.stream()
                                .filter(ur -> themeQuestionId.equals(ur.getQuestionId()))
                                .findFirst()
                                .orElse(null);

                        String themeValue = null;
                        if (themeUserResponse != null) {
                            // log.debug("Réponse à la question 'Thème' trouvée pour l'utilisateur {}. Extraction de la valeur.", userId);
                            System.out.println("Réponse à la question 'Thème' trouvée pour l'utilisateur " + userId + ". Extraction de la valeur.");
                            switch (themeQuestionType) {
                                case "Texte":
                                    themeValue = themeUserResponse.getTextResponse();
                                    break;
                                case "Réponse unique":
                                    themeValue = themeUserResponse.getSingleChoiceResponse();
                                    break;
                                case "Réponse multiple":
                                    if (themeUserResponse.getMultipleChoiceResponse() != null && !themeUserResponse.getMultipleChoiceResponse().isEmpty()) {
                                        themeValue = String.join(", ", themeUserResponse.getMultipleChoiceResponse());
                                    }
                                    break;
                                default:
                                    // log.warn("Type de question non géré ('{}') pour la question 'Thème' (ID: {}). Tentative d'extraction depuis les champs communs pour l'utilisateur {}.", themeQuestionType, themeQuestionId, userId);
                                    System.out.println("Attention: Type de question non géré ('" + themeQuestionType +
                                            "') pour la question 'Thème'. Tentative d'extraction depuis les champs communs pour l'utilisateur " + userId);
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
                            // log.warn("Réponse au 'Thème' non trouvée ou vide pour l'utilisateur {} et la question {} (ID: {}). Création de Besoin annulée.", userId, themeQuestionDefinition.getText(), themeQuestionId);
                            System.out.println("Attention: Réponse au 'Thème' non trouvée ou vide pour l'utilisateur " + userId +
                                    " et la question " + themeQuestionId + ". Création de Besoin annulée.");
                            operationStatusMessage += " La réponse à la question 'Thème' est vide ou non trouvée, le Besoin n'a pas été créé.";
                        } else {
                            // log.info("Réponse au 'Thème' ('{}') trouvée pour l'utilisateur {}. Création du Besoin.", themeValue, userId);
                            System.out.println("Réponse au 'Thème' ('" + themeValue + "') trouvée pour l'utilisateur " + userId + ". Création du Besoin.");

                            String requesterName = userDto.getName(); // Assurez-vous que UserDto a une méthode getName() ou un champ équivalent
                            Long approverId = userDto.getManagerId();   // Assurez-vous que UserDto a une méthode getManagerId() ou un champ équivalent
                            Long companyId = questionnaire.getCompanyId(); // Ou themeUserResponse.getCompanyId() si pertinent

                            Need need = Need.builder()
                                    .requesterId(userId)
                                    .requesterName(requesterName)
                                    .approverId(approverId)
                                    .source(NeedSource.Evaluation) // Assurez-vous que cet enum existe et est correctement importé
                                    .creationDate(DateTimeFormatter.ISO_LOCAL_DATE.format(nowDate)) // Format "YYYY-MM-DD"
                                    .type("Evaluation") // Ou un autre type si nécessaire
                                    .theme(themeValue)
                                    .questionnaire(questionnaire.getTitle())
                                    .companyId(companyId)
                                    .status(NeedStatusEnums.DRAFT) // Assurez-vous que cet enum existe et est correctement importé
                                    // Initialisez les autres champs requis de "Need" avec des valeurs par défaut ou null si permis
                                    .build();

                            needRepository.save(need);
                            // log.info("Besoin (Need) créé avec succès pour l'utilisateur {} avec le thème: {}. ID du Besoin: {}", userId, themeValue, need.getId());
                            System.out.println("Besoin (Need) créé pour l'utilisateur " + userId + " avec le thème: " + themeValue);
                            operationStatusMessage += " Un Besoin a été créé avec succès.";
                        }
                    }
                } else {
                    // log.info("Le questionnaire '{}' (ID: {}) n'est pas de type 'Récensement des besoins de formation'. Aucune création de Besoin.", questionnaire.getTitle(), questionnaireUuid);
                    System.out.println("Le questionnaire '" + questionnaire.getTitle() + "' n'est pas de type 'Récensement des besoins de formation'. Aucune création de Besoin.");
                    operationStatusMessage += " Le questionnaire n'est pas du type requis pour la création de Besoin.";
                }
            }
        }

        return ResponseEntity.ok().body(operationStatusMessage);
    }

    @Override
    public ResponseEntity<?> getAllUserQuestionsResponses(Long userId, UUID questionnaireId) {
        UserQuestionsResponsesDto userQuestionsResponsesDtos = new UserQuestionsResponsesDto();


        List<GetUserResponsesDto> getUserResponsesDtos = new ArrayList<>();

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId).get();


        List<Question> questions = questionnaire.getQuestions();

        List<QuestionDto> questionDtos = new ArrayList<>();

        questions.forEach(question -> {
            questionDtos.add(EvaluationUtilMethods.mapToQuestionDto(question));
        });

        List<UserResponse> userResponses = userResponseRepository.findByUserIdAndQuestionnaireId(userId, questionnaireId);
        userResponses.forEach(userResponse -> {
            getUserResponsesDtos.add(EvaluationUtilMethods.mapToGetUserResponsesDto(userResponse));
        });

        // Récupérer le nombre total de questions du questionnaire
        int totalQuestions = questionnaire.getQuestions().size();
        int numberOfValidResponses = 0;

        // Compter le nombre de réponses valides
        for (UserResponse userResponse : userResponses) {
            switch (userResponse.getResponseType()) {
                case "Score":
                    if (userResponse.getScoreResponse() != null) {
                        numberOfValidResponses++;
                    }
                    break;
                case "Notation":
                    if (userResponse.getRatingResponse() != null) {
                        numberOfValidResponses++;
                    }
                    break;
                case "Texte":
                    if (userResponse.getTextResponse() != null) {
                        numberOfValidResponses++;
                    }
                    break;
                case "Commentaire":
                    if (userResponse.getCommentResponse() != null) {
                        numberOfValidResponses++;
                    }
                    break;
                case "Réponse multiple":
                    if (userResponse.getMultipleChoiceResponse() != null && !userResponse.getMultipleChoiceResponse().isEmpty()) {
                        numberOfValidResponses++;
                    }
                    break;
                case "Réponse unique":
                    if (userResponse.getSingleChoiceResponse() != null) {
                        numberOfValidResponses++;
                    }
                    break;
                case "Evaluation":
                    if (userResponse.getSingleLevelChoiceResponse() != null) {
                        numberOfValidResponses++;
                    }
                    // Nous traiterons ce cas plus tard
                    break;
                default:
                    log.warn("Type de réponse non géré : {}", userResponse.getResponseType());
                    break;
            }
        }

        // Calculer la progression
        int progression = 0;
        if (totalQuestions > 0) {
            progression = (numberOfValidResponses * 100) / totalQuestions;
        }

        userQuestionsResponsesDtos.setId(questionnaireId);
        userQuestionsResponsesDtos.setTitle(questionnaire.getTitle());
        userQuestionsResponsesDtos.setType(questionnaire.getType());
        userQuestionsResponsesDtos.setDescription(questionnaire.getDescription() != null ? questionnaire.getDescription() : "");
        userQuestionsResponsesDtos.setQuestions(questionDtos);
        userQuestionsResponsesDtos.setResponses(getUserResponsesDtos);
        userQuestionsResponsesDtos.setCreationDate(questionnaire.getCreationDate().toString());
        userQuestionsResponsesDtos.setProgress(progression);
        return ResponseEntity.ok().body(userQuestionsResponsesDtos);
    }
}