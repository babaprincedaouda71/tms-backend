package org.example.trainingservice.service.plan;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.trainingservice.client.users.AuthServiceClient;
import org.example.trainingservice.dto.group.*;
import org.example.trainingservice.dto.need.DepartmentDto;
import org.example.trainingservice.dto.need.SiteDto;
import org.example.trainingservice.dto.ocf.OCFAddOrEditGroupDto;
import org.example.trainingservice.dto.plan.ParticipantForCancel;
import org.example.trainingservice.dto.plan.SendInvitationDto;
import org.example.trainingservice.entity.OCF;
import org.example.trainingservice.entity.TrainerForTrainingGroupe;
import org.example.trainingservice.entity.plan.Plan;
import org.example.trainingservice.entity.plan.Training;
import org.example.trainingservice.entity.plan.TrainingGroupe;
import org.example.trainingservice.enums.GroupeStatusEnums;
import org.example.trainingservice.enums.TrainingType;
import org.example.trainingservice.exceptions.GroupeNotFoundException;
import org.example.trainingservice.exceptions.TrainingGroupeNotFoundException;
import org.example.trainingservice.exceptions.TrainingNotFoundException;
import org.example.trainingservice.exceptions.plan.ValidationException;
import org.example.trainingservice.repository.OCFRepository;
import org.example.trainingservice.repository.TrainerForTrainingGroupeRepository;
import org.example.trainingservice.repository.plan.TrainingGroupeRepository;
import org.example.trainingservice.repository.plan.TrainingRepository;
import org.example.trainingservice.service.completion.CompletionUtilMethods;
import org.example.trainingservice.utils.SecurityUtils;
import org.example.trainingservice.utils.TrainingGroupeUtilMethods;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrainingGroupeServiceImpl implements TrainingGroupeService {
    private final TrainingGroupeRepository trainingGroupeRepository;
    private final TrainingRepository trainingRepository;
    private final OCFRepository ocfRepository;
    private final TrainerForTrainingGroupeRepository trainerRepository;
    private final TrainingGroupeCompletionService completionService;
    private final TrainingCompletionService trainingCompletionService;
    private final GroupeCompletionService groupeCompletionService;
    private final CompletionUtilMethods completionUtilMethods;
    private final TrainingInvitationService trainingInvitationService;
    private final AuthServiceClient authServiceClient;

    public TrainingGroupeServiceImpl(
            TrainingGroupeRepository trainingGroupeRepository,
            TrainingRepository trainingRepository,
            OCFRepository ocfRepository,
            TrainerForTrainingGroupeRepository trainerRepository,
            TrainingGroupeCompletionService completionService,
            TrainingCompletionService trainingCompletionService,
            GroupeCompletionService groupeCompletionService,
            CompletionUtilMethods completionUtilMethods,
            TrainingInvitationService trainingInvitationService,
            AuthServiceClient authServiceClient) {
        this.trainingGroupeRepository = trainingGroupeRepository;
        this.trainingRepository = trainingRepository;
        this.ocfRepository = ocfRepository;
        this.trainerRepository = trainerRepository;
        this.completionService = completionService;
        this.trainingCompletionService = trainingCompletionService;
        this.groupeCompletionService = groupeCompletionService;
        this.completionUtilMethods = completionUtilMethods;
        this.trainingInvitationService = trainingInvitationService;
        this.authServiceClient = authServiceClient;
    }

    @Override
    public ResponseEntity<?> getTrainingGroupToAddOrEdit(Long groupId) {
        TrainingGroupe found = trainingGroupeRepository.findById(groupId)
                .orElseThrow(() -> new GroupeNotFoundException("Groupe non trouvé avec l'ID : " + groupId, null));

        // Training
        Training training = found.getTraining();
        boolean isTrainingComplete = training.isComplete();

        // Plan
        Plan plan = training.getPlan();
        Boolean isOFPPTValidationEnabled = plan.getIsOFPPTValidation();
        return ResponseEntity.ok().body(TrainingGroupeUtilMethods.convertToTrainingGroupToAddOrEditDto(found, isTrainingComplete, isOFPPTValidationEnabled));
    }

    @Override
    public ResponseEntity<?> addGroupPlanning(UUID trainingId, AddOrEditGroupPlanningDto addOrEditGroupPlanningDto) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Training training = trainingRepository.findById(trainingId).orElseThrow(() -> new TrainingNotFoundException("Training not found with id: " + trainingId, null));

        // Création du groupe
        TrainingGroupe trainingGroupe = TrainingGroupe.builder()
                .training(training)
                .companyId(companyId)
                .siteIds(addOrEditGroupPlanningDto.getSite().stream().map(SiteDto::getId).collect(Collectors.toList()))
                .departmentIds(addOrEditGroupPlanningDto.getDepartment().stream().map(DepartmentDto::getId).collect(Collectors.toList()))
                .location(addOrEditGroupPlanningDto.getLocation())
                .city(addOrEditGroupPlanningDto.getCity())
                .dates(addOrEditGroupPlanningDto.getDates())
                .morningStartTime(addOrEditGroupPlanningDto.getMorningStartTime())
                .morningEndTime(addOrEditGroupPlanningDto.getMorningEndTime())
                .afternoonStartTime(addOrEditGroupPlanningDto.getAfternoonStartTime())
                .afternoonEndTime(addOrEditGroupPlanningDto.getAfternoonEndTime())
                .name("Groupe " + ((training.getNumberOfGroup()) + 1))
                .dayCount(addOrEditGroupPlanningDto.getDates().size())
                .status(GroupeStatusEnums.Brouillon)
                .build();

        // Vérifier la complétude et mettre à jour le champ isAllFieldsFilled
        trainingGroupe = groupeCompletionService.updateCompletionStatus(trainingGroupe);

        TrainingGroupe save = trainingGroupeRepository.save(trainingGroupe);

        // Mis à jour de la formation
        training.setNumberOfGroup(training.getNumberOfGroup() + 1);
        training.setSiteIds(trainingGroupe.getSiteIds());
        training.setSiteNames(addOrEditGroupPlanningDto.getSite().stream().map(SiteDto::getLabel).collect(Collectors.toList()));
        training.setDepartmentIds(trainingGroupe.getDepartmentIds());
        training.setDepartmentNames(addOrEditGroupPlanningDto.getDepartment().stream().map(DepartmentDto::getName).collect(Collectors.toList()));
        trainingRepository.save(training);

        // Vérifier la complétude de la formation après modification du groupe
        completionUtilMethods.checkAndUpdateTrainingCompletion(save);

        // Mapper le groupe en DTO
        GroupToAddOrEditDto groupToAddOrEditDto = TrainingGroupeUtilMethods.convertToGroupToAddOrEditDto(save);
        return ResponseEntity.ok().body(groupToAddOrEditDto);
    }

    @Override
    public ResponseEntity<?> editGroupPlanning(Long groupId, AddOrEditGroupPlanningDto addOrEditGroupPlanningDto) {
        // Récupération du groupe
        TrainingGroupe trainingGroupe = trainingGroupeRepository.findById(groupId).orElseThrow(() -> new TrainingGroupeNotFoundException("Training groupe not found with ID : " + groupId, null));

        // Extraction du training
        Training training = trainingGroupe.getTraining();

        // Mise à jour du groupe
        trainingGroupe.setSiteIds(addOrEditGroupPlanningDto.getSite().stream().map(SiteDto::getId).collect(Collectors.toList()));
        trainingGroupe.setDepartmentIds(addOrEditGroupPlanningDto.getDepartment().stream().map(DepartmentDto::getId).collect(Collectors.toList()));
        trainingGroupe.setLocation(addOrEditGroupPlanningDto.getLocation());
        trainingGroupe.setCity(addOrEditGroupPlanningDto.getCity());
        trainingGroupe.setDates(addOrEditGroupPlanningDto.getDates());
        trainingGroupe.setMorningStartTime(addOrEditGroupPlanningDto.getMorningStartTime());
        trainingGroupe.setMorningEndTime(addOrEditGroupPlanningDto.getMorningEndTime());
        trainingGroupe.setAfternoonStartTime(addOrEditGroupPlanningDto.getAfternoonStartTime());
        trainingGroupe.setAfternoonEndTime(addOrEditGroupPlanningDto.getAfternoonEndTime());
        trainingGroupe.setDayCount(addOrEditGroupPlanningDto.getDates().size());

        // Vérifier la complétude et mettre à jour le champ isAllFieldsFilled
        boolean wasComplete = trainingGroupe.getIsAllFieldsFilled();
        trainingGroupe = groupeCompletionService.updateCompletionStatus(trainingGroupe);
        boolean isNowComplete = trainingGroupe.getIsAllFieldsFilled();

        TrainingGroupe savedGroupe = trainingGroupeRepository.save(trainingGroupe);

        // Mise à jour de la formation
        training.setSiteIds(trainingGroupe.getSiteIds());
        training.setDepartmentIds(trainingGroupe.getDepartmentIds());
        training.setSiteNames(addOrEditGroupPlanningDto.getSite().stream().map(SiteDto::getLabel).collect(Collectors.toList()));
        training.setDepartmentNames(addOrEditGroupPlanningDto.getDepartment().stream().map(DepartmentDto::getName).collect(Collectors.toList()));
        trainingRepository.save(training);

        // Vérifier la complétude de la formation après modification du groupe
        completionUtilMethods.checkAndUpdateTrainingCompletion(savedGroupe);

        return ResponseEntity.ok().body(TrainingGroupeUtilMethods.convertToGroupToAddOrEditDto(savedGroupe));
    }

    @Override
    public ResponseEntity<?> addGroupParticipants(UUID trainingId, AddOrEditGroupParticipantsDto addOrEditGroupParticipantsDto) {
        Long companyId = SecurityUtils.getCurrentCompanyId();

        Training training = trainingRepository.findById(trainingId).orElseThrow(() -> new TrainingNotFoundException("Training not found with ID : " + trainingId, null));

        TrainingGroupe trainingGroupe = TrainingGroupe.builder()
                .training(training)
                .companyId(companyId)
                .name("Groupe " + ((training.getNumberOfGroup()) + 1))
                .targetAudience(addOrEditGroupParticipantsDto.getTargetAudience())
                .managerCount(addOrEditGroupParticipantsDto.getManagerCount())
                .employeeCount(addOrEditGroupParticipantsDto.getEmployeeCount())
                .workerCount(addOrEditGroupParticipantsDto.getWorkerCount())
                .temporaryWorkerCount(addOrEditGroupParticipantsDto.getTemporaryWorkerCount())
                .userGroupIds(addOrEditGroupParticipantsDto.getUserGroupIds())
                .participantCount(TrainingGroupeUtilMethods.calculateTotalParticipants(addOrEditGroupParticipantsDto))
                .build();

        // Création des invitations si non existante
        if (addOrEditGroupParticipantsDto.getUserGroupIds() != null && !addOrEditGroupParticipantsDto.getUserGroupIds().isEmpty()) {
            trainingInvitationService.createTrainingInvitation(trainingGroupe, addOrEditGroupParticipantsDto.getUserGroupIds());
        }

        // Vérifier la complétude et mettre à jour le champ isAllFieldsFilled
        trainingGroupe = groupeCompletionService.updateCompletionStatus(trainingGroupe);

        TrainingGroupe save = trainingGroupeRepository.save(trainingGroupe);

        training.setNumberOfGroup(training.getNumberOfGroup() + 1);
        trainingRepository.save(training);

        // Vérifier la complétude de la formation après modification du groupe
        completionUtilMethods.checkAndUpdateTrainingCompletion(save);

        return ResponseEntity.ok().body(TrainingGroupeUtilMethods.convertToGroupToAddOrEditDto(save));
    }

    @Override
    public ResponseEntity<?> editGroupParticipants(Long groupId, AddOrEditGroupParticipantsDto addOrEditGroupParticipantsDto) {
        // 1 Récupération du groupe
        TrainingGroupe trainingGroupe = trainingGroupeRepository.findById(groupId).orElseThrow(() -> new TrainingGroupeNotFoundException("Training Groupe not found with ID : " + groupId, null));

        // 2 Création des invitations si non existante
        if (addOrEditGroupParticipantsDto.getUserGroupIds() != null && !addOrEditGroupParticipantsDto.getUserGroupIds().isEmpty()) {
            trainingInvitationService.createTrainingInvitation(trainingGroupe, addOrEditGroupParticipantsDto.getUserGroupIds());
        }
        trainingGroupe.setTargetAudience(addOrEditGroupParticipantsDto.getTargetAudience());
        trainingGroupe.setManagerCount(addOrEditGroupParticipantsDto.getManagerCount());
        trainingGroupe.setEmployeeCount(addOrEditGroupParticipantsDto.getEmployeeCount());
        trainingGroupe.setWorkerCount(addOrEditGroupParticipantsDto.getWorkerCount());
        trainingGroupe.setTemporaryWorkerCount(addOrEditGroupParticipantsDto.getTemporaryWorkerCount());
        log.error("Participant ids: {}", addOrEditGroupParticipantsDto.getUserGroupIds());
        trainingGroupe.setUserGroupIds(addOrEditGroupParticipantsDto.getUserGroupIds());
        trainingGroupe.setParticipantCount(TrainingGroupeUtilMethods.calculateTotalParticipants(addOrEditGroupParticipantsDto));

        // Vérifier la complétude et mettre à jour le champ isAllFieldsFilled
        boolean wasComplete = trainingGroupe.getIsAllFieldsFilled();
        trainingGroupe = groupeCompletionService.updateCompletionStatus(trainingGroupe);
        boolean isNowComplete = trainingGroupe.getIsAllFieldsFilled();

        TrainingGroupe save = trainingGroupeRepository.save(trainingGroupe);

        // Vérifier la complétude de la formation après modification du groupe
        completionUtilMethods.checkAndUpdateTrainingCompletion(save);
        return ResponseEntity.ok().body(TrainingGroupeUtilMethods.convertToGroupToAddOrEditDto(save));
    }

    @Override
    public ResponseEntity<?> addGroupInternalProvider(UUID trainingId, AddOrEditGroupInternalProviderDto addOrEditGroupInternalProviderDto) {
        log.info("addGroupInternalProvider {}", addOrEditGroupInternalProviderDto);
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Training training = trainingRepository.findById(trainingId).orElseThrow(() -> new TrainingNotFoundException("Training not found with ID : " + trainingId, null));
        TrainingGroupe trainingGroupe = TrainingGroupe.builder()
                .training(training)
                .companyId(companyId)
                .name("Groupe " + ((training.getNumberOfGroup()) + 1))
                .internalTrainerId(addOrEditGroupInternalProviderDto.getTrainer().getId())
                .trainerName(addOrEditGroupInternalProviderDto.getTrainer().getName())
                .comment(addOrEditGroupInternalProviderDto.getComment())
                .trainingType(TrainingType.Interne)
                .build();

        // Vérifier la complétude et mettre à jour le champ isAllFieldsFilled
        trainingGroupe = groupeCompletionService.updateCompletionStatus(trainingGroupe);

        TrainingGroupe save = trainingGroupeRepository.save(trainingGroupe);
        training.setNumberOfGroup(training.getNumberOfGroup() + 1);
        trainingRepository.save(training);

        // Vérifier la complétude de la formation après modification du groupe
        completionUtilMethods.checkAndUpdateTrainingCompletion(save);
        return ResponseEntity.ok().body(TrainingGroupeUtilMethods.convertToGroupToAddOrEditDto(save));
    }

    @Override
    public ResponseEntity<?> editGroupInternalProvider(Long groupId, AddOrEditGroupInternalProviderDto addOrEditGroupInternalProviderDto) {
        // Implementation pour la modification d'un groupe avec un fournisseur interne
        log.info("editGroupInternalProvider {}", addOrEditGroupInternalProviderDto);
        TrainingGroupe trainingGroupe = trainingGroupeRepository.findById(groupId).orElseThrow(() -> new TrainingGroupeNotFoundException("Training Groupe not found with ID : " + groupId, null));
        trainingGroupe.setInternalTrainerId(addOrEditGroupInternalProviderDto.getTrainer().getId());
        trainingGroupe.setComment(addOrEditGroupInternalProviderDto.getComment());
        trainingGroupe.setTrainerName(addOrEditGroupInternalProviderDto.getTrainer().getName());
        trainingGroupe.setTrainingType(TrainingType.Interne);

        // Vérifier la complétude et mettre à jour le champ isAllFieldsFilled
        boolean wasComplete = trainingGroupe.getIsAllFieldsFilled();
        trainingGroupe = groupeCompletionService.updateCompletionStatus(trainingGroupe);
        boolean isNowComplete = trainingGroupe.getIsAllFieldsFilled();

        TrainingGroupe save = trainingGroupeRepository.save(trainingGroupe);

        // Vérifier la complétude de la formation après modification du groupe
        completionUtilMethods.checkAndUpdateTrainingCompletion(save);
        return ResponseEntity.ok().body(TrainingGroupeUtilMethods.convertToGroupToAddOrEditDto(save));
    }

    @Override
    public ResponseEntity<?> addGroupExternalProvider(UUID trainingId, AddOrEditGroupExternalProviderDto addOrEditGroupExternalProviderDto) {
        log.info("addGroupExternalProvider - trainingId: {}, dto: {}", trainingId, addOrEditGroupExternalProviderDto);
        Long companyId = SecurityUtils.getCurrentCompanyId();


        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new TrainingNotFoundException("Formation non trouvé avec l'ID : " + trainingId, null));

        TrainingGroupe trainingGroupe = new TrainingGroupe();
        trainingGroupe.setCompanyId(companyId);
        trainingGroupe.setName("Groupe " + ((training.getNumberOfGroup()) + 1));
        trainingGroupe.setTraining(training);
        trainingGroupe.setCompanyId(training.getCompanyId());
        trainingGroupe.setTrainingType(TrainingType.Externe);

        if (addOrEditGroupExternalProviderDto.getOcf() != null) {
            OCFAddOrEditGroupDto ocfDto = addOrEditGroupExternalProviderDto.getOcf();
            OCF ocf = ocfRepository.findById(ocfDto.getId()).orElse(null);
            if (ocf != null) {
                trainingGroupe.setOcf(ocf);
                log.info("OCF existant associé au groupe : {}", ocf);
            } else if (ocfDto.getCorporateName() != null && !ocfDto.getCorporateName().isEmpty()) {
                OCF newOcf = new OCF();
                newOcf.setCorporateName(ocfDto.getCorporateName());
                newOcf.setEmailMainContact(ocfDto.getEmailMainContact() != null && !ocfDto.getEmailMainContact().isEmpty() ? ocfDto.getEmailMainContact() : null);
                ocfRepository.save(newOcf);
                trainingGroupe.setOcf(newOcf);
                log.info("Nouvel OCF créé et associé au groupe : {}", newOcf);
            } else {
                log.warn("Aucun OCF existant trouvé avec l'ID {} et le nom de l'entreprise OCF n'est pas fourni.", ocfDto.getId());
            }
        } else {
            log.info("Aucune information OCF fournie dans le DTO.");
        }

        if (addOrEditGroupExternalProviderDto.getExternalTrainerName() != null && !addOrEditGroupExternalProviderDto.getExternalTrainerName().isEmpty()) {
            TrainerForTrainingGroupe existingTrainer = trainerRepository.findByNameAndEmail(
                    addOrEditGroupExternalProviderDto.getExternalTrainerName(),
                    addOrEditGroupExternalProviderDto.getExternalTrainerEmail()
            ).orElse(null);

            if (existingTrainer != null) {
                trainingGroupe.setTrainer(existingTrainer);
                trainingGroupe.setTrainerName(existingTrainer.getName());
                log.info("Formateur externe existant associé au groupe : {}", existingTrainer);
            } else {
                TrainerForTrainingGroupe newTrainer = new TrainerForTrainingGroupe();
                newTrainer.setName(addOrEditGroupExternalProviderDto.getExternalTrainerName());
                newTrainer.setEmail(addOrEditGroupExternalProviderDto.getExternalTrainerEmail());
                trainerRepository.save(newTrainer);
                trainingGroupe.setTrainer(newTrainer);
                trainingGroupe.setTrainerName(newTrainer.getName());
                log.info("Nouveau formateur externe créé et associé au groupe : {}", newTrainer);
            }
        } else {
            log.info("Aucune information sur le formateur externe fournie dans le DTO.");
        }

        trainingGroupe.setPrice(addOrEditGroupExternalProviderDto.getCost());

        // Vérifier la complétude et mettre à jour le champ isAllFieldsFilled
        trainingGroupe = groupeCompletionService.updateCompletionStatus(trainingGroupe);

        TrainingGroupe savedTrainingGroup = trainingGroupeRepository.save(trainingGroupe);
        log.info("Training Groupe externe ajouté avec succès : {}", savedTrainingGroup);

        // Mise à jour du numberOfGroup dans l'entité Need
        training.setNumberOfGroup(training.getNumberOfGroup() + 1);
        trainingRepository.save(training);

        // Vérifier la complétude de la formation après modification du groupe
        completionUtilMethods.checkAndUpdateTrainingCompletion(savedTrainingGroup);

        return ResponseEntity.ok().body(TrainingGroupeUtilMethods.convertToGroupToAddOrEditDto(savedTrainingGroup));
    }

    @Override
    public ResponseEntity<?> editGroupExternalProvider(Long groupId, AddOrEditGroupExternalProviderDto addOrEditGroupExternalProviderDto) {
        // Implementation pour la modification d'un groupe avec un fournisseur externe
        log.info("Starting updating training group external provider : {}", addOrEditGroupExternalProviderDto);
        TrainingGroupe trainingGroupe = trainingGroupeRepository.findById(groupId).orElseThrow(() -> new TrainingGroupeNotFoundException("Training Groupe not found with ID : " + groupId, null));
        log.info("Getting OCF");
        OCFAddOrEditGroupDto ocfDto = addOrEditGroupExternalProviderDto.getOcf();
        OCF ocf = ocfRepository.findById(ocfDto.getId()).orElse(null);
        if (ocf != null) {
            trainingGroupe.setOcf(ocf);
            trainingGroupe.setPrice(addOrEditGroupExternalProviderDto.getCost());
            log.info("OCF existant associé au training groupe : {}", ocf);
        } else if (ocfDto.getCorporateName() != null && !ocfDto.getCorporateName().isEmpty()) {
            OCF newOcf = new OCF();
            newOcf.setCorporateName(ocfDto.getCorporateName());
            newOcf.setEmailMainContact(ocfDto.getEmailMainContact() != null && !ocfDto.getEmailMainContact().isEmpty() ? ocfDto.getEmailMainContact() : null);
            ocfRepository.save(newOcf);
            trainingGroupe.setOcf(newOcf);
            log.info("Nouvel OCF créé et associé au training groupe : {}", newOcf);
        } else {
            log.warn("Aucun OCF existant trouvé avec l'ID {} et le nom de l'entreprise OCF n'est pas fourni.", ocfDto.getId());
        }

        if (addOrEditGroupExternalProviderDto.getExternalTrainerName() != null && !addOrEditGroupExternalProviderDto.getExternalTrainerName().isEmpty()) {
            TrainerForTrainingGroupe existingTrainer = trainerRepository.findByNameAndEmail(
                    addOrEditGroupExternalProviderDto.getExternalTrainerName(),
                    addOrEditGroupExternalProviderDto.getExternalTrainerEmail()
            ).orElse(null);

            if (existingTrainer != null) {
                trainingGroupe.setTrainer(existingTrainer);
                trainingGroupe.setTrainerName(existingTrainer.getName());
                log.info("Formateur externe existant associé au training groupe : {}", existingTrainer);
            } else {
                TrainerForTrainingGroupe newTrainer = new TrainerForTrainingGroupe();
                newTrainer.setName(addOrEditGroupExternalProviderDto.getExternalTrainerName());
                newTrainer.setEmail(addOrEditGroupExternalProviderDto.getExternalTrainerEmail());
                newTrainer.setGroupesAnimated(List.of(trainingGroupe));
                trainerRepository.save(newTrainer);
                trainingGroupe.setTrainer(newTrainer);
                trainingGroupe.setTrainerName(newTrainer.getName());
                log.info("Nouveau formateur externe créé et associé au training groupe : {}", newTrainer);
            }
        } else {
            log.info("Aucune information sur le formateur externe fournie dans le DTO.");
        }

        trainingGroupe.setPrice(addOrEditGroupExternalProviderDto.getCost());
        trainingGroupe.setTrainingType(TrainingType.Externe);

        // Vérifier la complétude et mettre à jour le champ isAllFieldsFilled
        boolean wasComplete = trainingGroupe.getIsAllFieldsFilled();
        trainingGroupe = groupeCompletionService.updateCompletionStatus(trainingGroupe);
        boolean isNowComplete = trainingGroupe.getIsAllFieldsFilled();

        TrainingGroupe savedTrainingGroupe = trainingGroupeRepository.save(trainingGroupe);

        // Vérifier la complétude de la formation après modification du groupe
        completionUtilMethods.checkAndUpdateTrainingCompletion(savedTrainingGroupe);
        log.info("****** Groupe externe ajouté avec succès ******");
        return ResponseEntity.ok().body(TrainingGroupeUtilMethods.convertToGroupToAddOrEditDto(savedTrainingGroupe));
    }

    /*
    TODO : méthodes optionnelles pour la complétion des champs
    * **/
    // 4. Méthode à implémenter pour l'action spécifique
    private void performSpecificActionWhenComplete(TrainingGroupe trainingGroupe) {
        // Implémentez ici votre action spécifique quand tous les champs sont remplis
        log.info("Exécution de l'action spécifique pour le groupe avec tous les champs remplis: {}", trainingGroupe.getId());

        // Exemples d'actions possibles :
        // - Envoyer une notification
        // - Créer un événement
        // - Mettre à jour d'autres entités
        // - Déclencher un processus workflow
    }

    // 5. Méthode utilitaire pour obtenir un rapport détaillé (optionnel)
    @Override
    public ResponseEntity<?> getGroupCompletionStatus(Long groupId) {
        TrainingGroupe trainingGroupe = trainingGroupeRepository.findById(groupId)
                .orElseThrow(() -> new TrainingGroupeNotFoundException("Training groupe not found with ID : " + groupId, null));

        TrainingGroupeCompletionService.CompletionReport report = completionService.getCompletionReport(trainingGroupe);

        return ResponseEntity.ok().body(Map.of(
                "isAllFieldsFilled", trainingGroupe.getIsAllFieldsFilled(),
                "isComplete", report.isComplete(),
                "missingFields", report.getMissingFields(),
                "currentStatus", trainingGroupe.getStatus() // Le statut reste indépendant
        ));
    }

    @Override
    public ResponseEntity<?> sendInvitations(Long groupId, SendInvitationDto sendInvitationDto) {
        try {
            // Validation des données
            validateSendInvitationRequest(sendInvitationDto);

            Long companyId = SecurityUtils.getCurrentCompanyId();
            UUID trainingId = sendInvitationDto.getTrainingId();
            Set<Long> participantIds = sendInvitationDto.getParticipantIds();
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public ResponseEntity<?> getParticipantsForTrainingInvitation(Long groupId) {
        log.info("getParticipantsForTrainingInvitation");
        try {
            // Récupérer le groupe
            TrainingGroupe trainingGroupe = trainingGroupeRepository.findById(groupId).orElseThrow(() -> new TrainingGroupeNotFoundException("Training Groupe not found with ID : " + groupId, null));

            // Collecte des participants ids
            Set<Long> participantIds = trainingGroupe.getUserGroupIds();

            // Vérification pour s'assurer que ce n'est pas vide
            if (participantIds.isEmpty()) {
                log.info("No participants founds");
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Récupération des infos des users
            List<ParticipantForCancel> allParticipants = new ArrayList<>();

            allParticipants = authServiceClient.getParticipantsEmail(participantIds);

            return ResponseEntity.ok(allParticipants);
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    /**
     * Valide les données d'entrée pour l'annulation de formation
     */
    private void validateSendInvitationRequest(SendInvitationDto sendInvitationDto) {
        if (sendInvitationDto == null) {
            throw new ValidationException("Les données de l'invitation sont requises");
        }

        if (sendInvitationDto.getTrainingId() == null) {
            throw new ValidationException("L'ID de la formation est requis");
        }

        if (sendInvitationDto.getParticipantIds() == null || sendInvitationDto.getParticipantIds().isEmpty()) {
            throw new ValidationException("Au moins un participant doit être spécifié");
        }

        if (sendInvitationDto.getParticipantIds().size() > MAX_PARTICIPANTS_PER_BATCH) {
            throw new ValidationException("Trop de participants pour une annulation en lot (max: " + MAX_PARTICIPANTS_PER_BATCH + ")");
        }

        if (StringUtils.isBlank(sendInvitationDto.getContent())) {
            throw new ValidationException("Le message d'annulation est requis");
        }
    }

    // Constantes pour la gestion des performances
    private static final int MAX_PARTICIPANTS_PER_BATCH = 1000;
    private static final int PARTICIPANTS_BATCH_SIZE = 100;
    private static final long BATCH_PROCESSING_DELAY_MS = 50;
}