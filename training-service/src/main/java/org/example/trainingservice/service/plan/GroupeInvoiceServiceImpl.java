package org.example.trainingservice.service.plan;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.dto.plan.AddGroupeInvoiceDto;
import org.example.trainingservice.entity.plan.GroupeInvoice;
import org.example.trainingservice.entity.plan.TrainingGroupe;
import org.example.trainingservice.exceptions.TrainingGroupeNotFoundException;
import org.example.trainingservice.repository.plan.GroupeInvoiceRepository;
import org.example.trainingservice.repository.plan.TrainingGroupeRepository;
import org.example.trainingservice.utils.GroupeInvoiceUtilMethods;
import org.example.trainingservice.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class GroupeInvoiceServiceImpl implements GroupeInvoiceService {
    private final GroupeInvoiceRepository groupeInvoiceRepository;
    private final TrainingGroupeRepository trainingGroupeRepository;
    private final FileStorageService fileStorageService;

    public GroupeInvoiceServiceImpl(GroupeInvoiceRepository groupeInvoiceRepository, TrainingGroupeRepository trainingGroupeRepository, FileStorageService fileStorageService) {
        this.groupeInvoiceRepository = groupeInvoiceRepository;
        this.trainingGroupeRepository = trainingGroupeRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public ResponseEntity<?> getAll(Long groupId) {
        log.info("Recherche des factures pour le groupe {}", groupId);

        // Vérifier que le groupe existe
        if (!trainingGroupeRepository.existsById(groupId)) {
            throw new TrainingGroupeNotFoundException("Groupe de formation non trouvé avec l'ID: " + groupId, null);
        }

        List<GroupeInvoice> invoices = groupeInvoiceRepository.findByTrainingGroupeId(groupId);
        return ResponseEntity.ok(GroupeInvoiceUtilMethods.mapToGetGroupeInvoicesDto(invoices));
    }

    @Override
    public ResponseEntity<?> getGroupeInvoice(UUID groupeInvoiceId) {
        log.info("Recherche de la facture pour l'ID {}", groupeInvoiceId);

        Optional<GroupeInvoice> foundGroupeInvoice = groupeInvoiceRepository.findById(groupeInvoiceId);
        if (foundGroupeInvoice.isPresent()) {
            GroupeInvoice groupeInvoice = foundGroupeInvoice.get();
            return ResponseEntity.ok(GroupeInvoiceUtilMethods.mapSingleInvoiceToDto(groupeInvoice));
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<?> addGroupeInvoice(
            Long groupId, AddGroupeInvoiceDto createDto,
            MultipartFile invoiceFile,
            MultipartFile bankRemiseFile,
            MultipartFile receiptFile
    ) {
        log.info("Ajout d'une facture pour le groupe {}", groupId);

        // Vérifier que le groupe existe
        // Vérifier que le groupe existe
        TrainingGroupe trainingGroupe = trainingGroupeRepository.findById(groupId)
                .orElseThrow(() -> new TrainingGroupeNotFoundException("Groupe de formation non trouvé avec l'ID: " + groupId, null));

        // Upload des fichiers vers MinIO
        String invoiceFileName = uploadFileIfPresent(invoiceFile, "invoice");
        String bankRemiseFileName = uploadFileIfPresent(bankRemiseFile, "bank-remise");
        String receiptFileName = uploadFileIfPresent(receiptFile, "receipt");

        try {
            // Créer l'entité GroupeInvoice
            GroupeInvoice invoice = GroupeInvoice.builder()
                    .trainingGroupe(trainingGroupe)
                    .companyId(SecurityUtils.getCurrentCompanyId())
                    .type(createDto.getType())
                    .creationDate(LocalDate.now())
                    .description(createDto.getDescription())
                    .amount(createDto.getAmount())
                    .status("Non Reglée")
                    .paymentDate(createDto.getPaymentDate() != null ? createDto.getPaymentDate() : LocalDate.now())
                    .paymentMethod(createDto.getPaymentMethod())
                    .invoiceFile(invoiceFileName)
                    .bankRemiseFile(bankRemiseFileName)
                    .receiptFile(receiptFileName)
                    .build();

            // Sauvegarder en base
            GroupeInvoice savedInvoice = groupeInvoiceRepository.save(invoice);

            log.info("Facture créée avec succès - ID: {}", savedInvoice.getId());

            // Convertir en DTO et retourner
            return ResponseEntity.ok(GroupeInvoiceUtilMethods.mapSingleInvoiceToDto(savedInvoice));

        } catch (Exception e) {
            // En cas d'erreur, supprimer les fichiers uploadés
            log.error("Erreur lors de la création de la facture, suppression des fichiers uploadés", e);
            deleteFileIfPresent(invoiceFileName);
            deleteFileIfPresent(bankRemiseFileName);
            deleteFileIfPresent(receiptFileName);
            throw new RuntimeException("Erreur lors de la création de la facture: " + e.getMessage(), e);
        }
    }

    /*************************************************************/
    /**
     * Upload un fichier vers MinIO si présent
     */
    private String uploadFileIfPresent(MultipartFile file, String prefix) {
        if (file != null && !file.isEmpty()) {
            log.debug("Upload du fichier {} de type {}", file.getOriginalFilename(), prefix);
            return fileStorageService.uploadFile(file);
        }
        return null;
    }

    /**
     * Supprimer un fichier de MinIO si présent
     */
    private void deleteFileIfPresent(String fileName) {
        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                fileStorageService.deleteFile(fileName);
                log.debug("Fichier supprimé: {}", fileName);
            } catch (Exception e) {
                log.warn("Erreur lors de la suppression du fichier {}: {}", fileName, e.getMessage());
            }
        }
    }

    /**
     * Récupérer le nom de fichier selon le type
     */
    private String getFileNameByType(GroupeInvoice invoice, String fileType) {
        return switch (fileType.toLowerCase()) {
            case "invoice", "facture" -> invoice.getInvoiceFile();
            case "bank-remise", "remise" -> invoice.getBankRemiseFile();
            case "receipt", "recu" -> invoice.getReceiptFile();
            default -> null;
        };
    }
    /*************************************************************/
}