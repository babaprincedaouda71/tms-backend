package org.example.trainingservice.service.ocf;

import org.example.trainingservice.dto.ocf.OCFCreateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface OCFService {
    ResponseEntity<?> getAllOcf();

    ResponseEntity<?> getOcfAddOrEditGroup();

    ResponseEntity<?> createOCF(OCFCreateDto ocfCreateDto, MultipartFile legalStatusFile, MultipartFile eligibilityCertificateFile, MultipartFile jrcTemplateFile, MultipartFile insurancePolicyFile, MultipartFile taxComplianceCertificateFile, MultipartFile bankStatementCertificateFile, MultipartFile termsAndConditionsFile);

    ResponseEntity<?> updateStatus(Long id);
}