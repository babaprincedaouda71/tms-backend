package org.example.trainingservice.web.OCF;

import org.example.trainingservice.dto.ocf.OCFCreateDto;
import org.example.trainingservice.service.ocf.OCFService;
import org.example.trainingservice.utils.FileUtilMethods;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocf")
public class OCFController {
    private final OCFService ocfService;

    public OCFController(OCFService ocfService) {
        this.ocfService = ocfService;
    }

    @GetMapping("/get/all")
    public ResponseEntity<?> getAll() {
        return ocfService.getAllOcf();
    }

    @GetMapping("/get/ocfAddOrEditGroup")
    public ResponseEntity<?> getOcfAddOrEditGroup() {
        return ocfService.getOcfAddOrEditGroup();
    }

    @PostMapping(path="/add/ocf", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createOcf(
            @RequestPart OCFCreateDto ocfCreateDto,
            @RequestPart(required = false)MultipartFile legalStatusFile,
            @RequestPart(required = false)MultipartFile eligibilityCertificateFile,
            @RequestPart(required = false)MultipartFile jrcTemplateFile,
            @RequestPart(required = false)MultipartFile insurancePolicyFile,
            @RequestPart(required = false)MultipartFile taxComplianceCertificateFile,
            @RequestPart(required = false)MultipartFile bankStatementCertificateFile,
            @RequestPart(required = false)MultipartFile termsAndConditionsFile
            ) {
        // Validation des fichiers pdf
        FileUtilMethods.validatePdfFiles(
                legalStatusFile,
                eligibilityCertificateFile,
                jrcTemplateFile,
                insurancePolicyFile,
                taxComplianceCertificateFile,
                bankStatementCertificateFile,
                termsAndConditionsFile
        );
        return ocfService.createOCF(
                ocfCreateDto,
                legalStatusFile,
                eligibilityCertificateFile,
                jrcTemplateFile,
                insurancePolicyFile,
                taxComplianceCertificateFile,
                bankStatementCertificateFile,
                termsAndConditionsFile
        );
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id) {
        return ocfService.updateStatus(id);
    }
}