package org.example.trainingservice.utils;

import org.example.trainingservice.dto.ocf.OCFAddOrEditGroupDto;
import org.example.trainingservice.dto.ocf.OCFDashboardDataDto;
import org.example.trainingservice.entity.OCF;

import java.util.List;
import java.util.stream.Collectors;

public class OCFUtilMethods {
    public static List<OCFAddOrEditGroupDto> mapToOCFAddOrEditGroupDto(List<OCF> ocfs) {
        return ocfs.stream().map(ocf -> {
            OCFAddOrEditGroupDto dto = new OCFAddOrEditGroupDto();
            dto.setId(ocf.getId());
            dto.setCorporateName(ocf.getCorporateName());
            dto.setEmailMainContact(ocf.getEmailMainContact());
            return dto;
        }).collect(Collectors.toList());
    }

    public static List<OCFDashboardDataDto> mapToOCFDashboardDataDto(List<OCF> ocfs) {
        return ocfs.stream().map(ocf -> {
            OCFDashboardDataDto dto = new OCFDashboardDataDto();
            dto.setId(ocf.getId());
            dto.setCompanyId(ocf.getCompanyId());
            dto.setCorporateName(ocf.getCorporateName());
            dto.setIce(ocf.getIce());
            dto.setPhone(ocf.getPhone());
            dto.setEmail(ocf.getEmail());
            dto.setAddress(ocf.getAddress());
            dto.setWebsite(ocf.getWebsite());
            dto.setNameMainContact(ocf.getNameMainContact());
            dto.setPositionMainContact(ocf.getPositionMainContact());
            dto.setEmailMainContact(ocf.getEmailMainContact());
            dto.setPhoneMainContact(ocf.getPhoneMainContact());
            dto.setNameLegalRepresentant(ocf.getNameLegalRepresentant());
            dto.setPositionLegalRepresentant(ocf.getPositionLegalRepresentant());
            dto.setEmailLegalRepresentant(ocf.getEmailLegalRepresentant());
            dto.setPhoneLegalRepresentant(ocf.getPhoneLegalRepresentant());
            return dto;
        }).collect(Collectors.toList());
    }
}