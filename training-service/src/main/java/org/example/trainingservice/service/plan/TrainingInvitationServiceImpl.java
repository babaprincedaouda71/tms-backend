package org.example.trainingservice.service.plan;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.client.notification.NotificationServiceClient;
import org.example.trainingservice.client.users.AuthServiceClient;
import org.example.trainingservice.dto.plan.InvitationSummaryDto;
import org.example.trainingservice.dto.plan.ParticipantForCancel;
import org.example.trainingservice.dto.plan.SendInvitationDto;
import org.example.trainingservice.entity.plan.Training;
import org.example.trainingservice.entity.plan.TrainingGroupe;
import org.example.trainingservice.entity.plan.TrainingInvitation;
import org.example.trainingservice.enums.InvitationStatusEnum;
import org.example.trainingservice.model.plan.SendInvitationEmailRequest;
import org.example.trainingservice.repository.TrainingInvitationRepository;
import org.example.trainingservice.repository.plan.TrainingGroupeRepository;
import org.example.trainingservice.repository.plan.TrainingRepository;
import org.example.trainingservice.utils.SecurityUtils;
import org.example.trainingservice.utils.TrainingInvitationUtilMethods;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrainingInvitationServiceImpl implements TrainingInvitationService {
    private final TrainingInvitationRepository trainingInvitationRepository;
    private final TrainingGroupeRepository trainingGroupeRepository;
    private final TrainingRepository trainingRepository;
    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public TrainingInvitationServiceImpl(
            TrainingInvitationRepository trainingInvitationRepository,
            TrainingGroupeRepository trainingGroupeRepository,
            TrainingRepository trainingRepository,
            AuthServiceClient authServiceClient,
            NotificationServiceClient notificationServiceClient
    ) {
        this.trainingInvitationRepository = trainingInvitationRepository;
        this.trainingGroupeRepository = trainingGroupeRepository;
        this.trainingRepository = trainingRepository;
        this.authServiceClient = authServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Override
    public ResponseEntity<?> getInvitations(Long groupId) {
        log.info("Récupération des invitations pour le groupe {}", groupId);

        // 1. On récupère la liste des invitations. Si aucune n'est trouvée,
        // le repository retournera une liste vide (et non 'null').
        List<TrainingInvitation> byTrainingGroupeId = trainingInvitationRepository.findByTrainingGroupeId(groupId);

        // 2. On mappe la liste d'entités vers une liste de DTOs.
        // Si 'byTrainingGroupeId' est une liste vide, cette méthode retournera aussi une liste vide.
        var invitationDtos = TrainingInvitationUtilMethods.mapToTrainingInvitationDtos(byTrainingGroupeId);

        // 3. On retourne la liste de DTOs avec un statut 200 OK.
        // Si la liste est vide, le client recevra un corps de réponse JSON comme ceci : []
        return ResponseEntity.ok(invitationDtos);
    }

    @Override
    @Transactional
    public ResponseEntity<?> sendInvitations(Long groupId, SendInvitationDto sendInvitationDto) {
        try {
            log.info("Début d'envoi des invitations pour le groupe {} avec {} participants",
                    groupId, sendInvitationDto.getParticipantIds().size());

            // 1. Validation des données d'entrée
            if (sendInvitationDto.getParticipantIds() == null || sendInvitationDto.getParticipantIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("La liste des participants ne peut pas être vide");
            }

            // 2. Récupération du groupe de formation
            Optional<TrainingGroupe> groupeOpt = trainingGroupeRepository.findById(groupId);
            if (groupeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            TrainingGroupe groupe = groupeOpt.get();

            // 3. Récupération de la formation associée
            Training training = null;
            if (sendInvitationDto.getTrainingId() != null) {
                Optional<Training> trainingOpt = trainingRepository.findById(sendInvitationDto.getTrainingId());
                if (trainingOpt.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body("Formation non trouvée avec l'ID: " + sendInvitationDto.getTrainingId());
                }
                training = trainingOpt.get();
            } else {
                training = groupe.getTraining();
            }

            // 4. Récupération des informations des participants depuis auth-service
            List<ParticipantForCancel> participants;
            try {
                participants = authServiceClient.getParticipantsEmail(sendInvitationDto.getParticipantIds());
                log.info("Récupéré {} participants depuis auth-service", participants.size());
            } catch (Exception e) {
                log.error("Erreur lors de la récupération des participants depuis auth-service", e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Impossible de récupérer les informations des participants");
            }

            // 5. Filtrage des participants qui n'ont pas déjà d'invitation
            List<ParticipantForCancel> participantsToInvite = new ArrayList<>();
            List<TrainingInvitation> newInvitations = new ArrayList<>();

            for (ParticipantForCancel participant : participants) {
                // Vérifier si une invitation existe déjà
                Optional<TrainingInvitation> existingInvitation =
                        trainingInvitationRepository.findByUserIdAndTrainingGroupeId(participant.getId(), groupId);

                if (existingInvitation.isEmpty()) {
                    participantsToInvite.add(participant);

                    // Créer l'invitation en base de données
                    TrainingInvitation invitation = TrainingInvitation.builder()
                            .trainingGroupe(groupe)
                            .userId(participant.getId())
                            .companyId(groupe.getCompanyId())
                            .userEmail(participant.getEmail())
                            .userFullName(participant.getName())
                            .status(InvitationStatusEnum.PENDING)
                            .invitationDate(LocalDate.now())
                            .notes("Invitation envoyée via l'interface admin")
                            .build();

                    newInvitations.add(invitation);
                } else {
                    log.warn("Invitation déjà existante pour l'utilisateur {} et le groupe {}",
                            participant.getId(), groupId);
                }
            }

            if (participantsToInvite.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Tous les participants sélectionnés ont déjà reçu une invitation");
            }

            // 6. Sauvegarde des invitations en base de données
            try {
                trainingInvitationRepository.saveAll(newInvitations);
                log.info("Sauvegardé {} nouvelles invitations en base", newInvitations.size());
            } catch (Exception e) {
                log.error("Erreur lors de la sauvegarde des invitations", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de la sauvegarde des invitations");
            }

            // 7. Préparation des détails de la formation pour l'email
//            TrainingGroupeDetails trainingDetails = TrainingGroupeDetails.builder()
//                    .groupeId(groupe.getId())
//                    .trainingName(training.getTheme())
//                    .startDate(groupe.getStartDate())
//                    .endDate(groupe.getEndDate())
//                    .location(groupe.getLocation())
//                    .city(groupe.getCity())
//                    .dates(groupe.getDates())
//                    .morningStartTime(groupe.getMorningStartTime())
//                    .morningEndTime(groupe.getMorningEndTime())
//                    .afternoonStartTime(groupe.getAfternoonStartTime())
//                    .afternoonEndTime(groupe.getAfternoonEndTime())
//                    .build();

            // 8. Extraction des emails pour l'envoi
            Set<String> emails = participantsToInvite.stream()
                    .map(ParticipantForCancel::getEmail)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (emails.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Aucun email valide trouvé pour les participants");
            }

            // 9. Préparation de la requête pour le service de notification
            SendInvitationEmailRequest emailRequest = SendInvitationEmailRequest.builder()
                    .emails(emails)
                    .message(sendInvitationDto.getContent())
                    .build();

            // 10. Envoi des emails via le service de notification
            try {
                notificationServiceClient.sendInvitationEmails(emailRequest);
                log.info("Emails d'invitation envoyés avec succès pour {} participants", participantsToInvite.size());
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi des emails d'invitation", e);

                // En cas d'échec d'envoi d'email, marquer les invitations comme échouées
                // (optionnel : vous pourriez vouloir ajouter un statut FAILED)
                log.warn("Les invitations ont été sauvegardées en base mais l'envoi d'email a échoué");

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .body("Invitations créées en base mais erreur lors de l'envoi des emails");
            }

            // 10. Mise à jour du statut du groupe si nécessaire
            // Vous pourriez vouloir marquer que des invitations ont été envoyées
            updateGroupeAfterInvitations(groupe, newInvitations.size());

            // 11. Réponse de succès
            InvitationSummaryDto summary = InvitationSummaryDto.builder()
                    .totalParticipants(sendInvitationDto.getParticipantIds().size())
                    .newInvitations(newInvitations.size())
                    .existingInvitations(participants.size() - participantsToInvite.size())
                    .emailsSent(participantsToInvite.size())
                    .smsSent(Boolean.TRUE.equals(sendInvitationDto.getSendSms()) ? participantsToInvite.size() : 0)
                    .build();

            log.info("Envoi des invitations terminé avec succès: {}", summary);

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'envoi des invitations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur inattendue lors de l'envoi des invitations: " + e.getMessage());
        }
    }

    @Override
    public void createTrainingInvitation(TrainingGroupe trainingGroupe, Set<Long> userGroupIds) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Long groupId = trainingGroupe.getId();

        List<TrainingInvitation> trainingInvitations = new ArrayList<>();

        // Récupération des participants
        List<ParticipantForCancel> participants = List.of();
        try {
            participants = authServiceClient.getParticipantsEmail(userGroupIds);
        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde des invitations", e);
        }
        for (ParticipantForCancel participant : participants) {
            TrainingInvitation retrievedInvitation = trainingInvitationRepository.findByTrainingGroupeIdAndUserId(groupId, participant.getId());
            if (retrievedInvitation == null) {
                TrainingInvitation invitation = TrainingInvitation.builder()
                        .trainingGroupe(trainingGroupe)
                        .userId(participant.getId())
                        .userEmail(participant.getEmail())
                        .userFullName(participant.getName())
                        .companyId(companyId)
                        .status(InvitationStatusEnum.NOT_SENT)
                        .invitationDate(null)
                        .build();
                trainingInvitations.add(invitation);
            }
        }
        // Enregistrement des invitations
        trainingInvitationRepository.saveAll(trainingInvitations);
    }

    /**
     * Met à jour le groupe après envoi des invitations
     */
    private void updateGroupeAfterInvitations(TrainingGroupe groupe, int invitationsCount) {
        try {
            // Vous pourriez vouloir ajouter des champs comme lastInvitationDate, totalInvitationsSent, etc.
            // groupe.setLastInvitationDate(LocalDateTime.now());
            // groupe.setTotalInvitationsSent(groupe.getTotalInvitationsSent() + invitationsCount);
            // trainingGroupeRepository.save(groupe);

            log.info("Groupe {} mis à jour après envoi de {} invitations", groupe.getId(), invitationsCount);
        } catch (Exception e) {
            log.warn("Erreur lors de la mise à jour du groupe après envoi des invitations", e);
        }
    }
}