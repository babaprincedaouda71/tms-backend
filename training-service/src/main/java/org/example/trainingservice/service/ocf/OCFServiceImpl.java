package org.example.trainingservice.service.ocf;

import org.example.trainingservice.entity.OCF;
import org.example.trainingservice.repository.OCFRepository;
import org.example.trainingservice.utils.OCFUtilMethods;
import org.example.trainingservice.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
}