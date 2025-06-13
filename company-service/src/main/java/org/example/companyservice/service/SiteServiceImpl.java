package org.example.companyservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.companyservice.entity.Site;
import org.example.companyservice.exceptions.SiteNotFoundException;
import org.example.companyservice.repository.SiteRepository;
import org.example.companyservice.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SiteServiceImpl implements SiteService {
    private final SiteRepository siteRepository;

    public SiteServiceImpl(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public ResponseEntity<?> getAll() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return ResponseEntity.ok(siteRepository.findAllByCompanyId(companyId));
    }

    @Override
    public ResponseEntity<?> getById(Long id) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Site found = siteRepository.findByIdAndCompanyId(id, companyId).orElseThrow(() -> new SiteNotFoundException("SITE NOT FOUND", null));
        return ResponseEntity.ok(found);
    }

    @Override
    public ResponseEntity<?> add(Site site) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        site.setCompanyId(companyId);
        return ResponseEntity.ok(siteRepository.save(site));
    }

    @Override
    public ResponseEntity<?> edit(Long id, Site site) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Site found = siteRepository.findByIdAndCompanyId(id, companyId).orElseThrow(() -> new SiteNotFoundException("SITE NOT FOUND", null));
        found.setLabel(site.getLabel());
        found.setAddress(site.getAddress());
        found.setCity(site.getCity());
        found.setPhone(site.getPhone());
        found.setTrainingRoom(site.getTrainingRoom());
        found.setSize(site.getSize());
        return ResponseEntity.ok(siteRepository.save(found));
    }

    @Override
    public ResponseEntity<?> delete(Long id) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        siteRepository.findByIdAndCompanyId(id, companyId).orElseThrow(() -> new SiteNotFoundException("SITE NOT FOUND", null));
        siteRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getSitesByIds(List<Long> ids) {
        return ResponseEntity.ok().body(siteRepository.findAllById(ids));
    }
}