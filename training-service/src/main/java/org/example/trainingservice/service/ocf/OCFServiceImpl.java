package org.example.trainingservice.service.ocf;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.dto.ocf.OCFCreateDto;
import org.example.trainingservice.entity.OCF;
import org.example.trainingservice.repository.OCFRepository;
import org.example.trainingservice.utils.OCFUtilMethods;
import org.example.trainingservice.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
public class OCFServiceImpl implements OCFService {
    private final OCFRepository ocfRepository;

    public OCFServiceImpl(OCFRepository ocfRepository) {
        this.ocfRepository = ocfRepository;
    }

    @Override
    public ResponseEntity<?> getAllOcf() {
        List<OCF> ocfs = ocfRepository.findByCompanyId(SecurityUtils.getCurrentCompanyId());
        return ResponseEntity.ok(OCFUtilMethods.mapToOCFDashboardDataDto(ocfs));
    }

    @Override
    public ResponseEntity<?> getOcfAddOrEditGroup() {
        List<OCF> byCompanyId = ocfRepository.findByCompanyId(SecurityUtils.getCurrentCompanyId());
        return ResponseEntity.ok(OCFUtilMethods.mapToOCFAddOrEditGroupDto(byCompanyId));
    }

    @Override
    public ResponseEntity<?> createOCF(
            OCFCreateDto ocfCreateDto,
            MultipartFile legalStatusFile,
            MultipartFile eligibilityCertificateFile,
            MultipartFile jrcTemplateFile,
            MultipartFile insurancePolicyFile,
            MultipartFile taxComplianceCertificateFile,
            MultipartFile bankStatementCertificateFile,
            MultipartFile termsAndConditionsFile
    ) {
        log.info("Starting creating OCF : {}", ocfCreateDto);

        Long companyId = SecurityUtils.getCurrentCompanyId();

        OCF ocf = OCFUtilMethods.mapToOCF(ocfCreateDto, companyId);

        ocfRepository.save(ocf);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> updateStatus(Long id) {
        return null;
    }
}