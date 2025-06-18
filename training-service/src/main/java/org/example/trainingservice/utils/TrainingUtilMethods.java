package org.example.trainingservice.utils;

import org.example.trainingservice.dto.group.GroupDto;
import org.example.trainingservice.dto.need.*;
import org.example.trainingservice.dto.plan.*;
import org.example.trainingservice.entity.plan.Training;

import java.util.List;
import java.util.stream.Collectors;

public class TrainingUtilMethods {
    public static GetAllTrainingDto mapToGetAllTrainingsDto(Training training) {
        return GetAllTrainingDto.builder()
                .id(training.getId())
                .theme(training.getTheme())
                .creationDate(training.getCreationDate())
                .type(training.getType())
                .csf(training.getCsf())
                .status(training.getStatus().getDescription())
                .build();
    }

    public static TrainingDetailsDto convertToTrainingDto(Training training) {
        TrainingDetailsDto dto = new TrainingDetailsDto();

        dto.setId(training.getId());
        if (training.getDomainId() != null) {
            dto.setDomain(training.getDomainName());
        }
        dto.setTheme(training.getTheme());
        if (training.getObjective() != null) {
            dto.setObjective(training.getObjective());
        }
        if (training.getContent() != null) {
            dto.setContent(training.getContent());
        }
        if (training.getCsf() != null) {
            dto.setCsf(training.getCsf());
            dto.setCsfPlanifie(training.getCsfPlanifie());
        }


        // Convertis la liste des entités Groupe en liste de GroupDto
        if (training.getGroupes() != null && !training.getGroupes().isEmpty()) {
            List<GroupDto> groupDtos = training.getGroupes().stream()
                    .map(TrainingGroupeUtilMethods::convertToGroupDto)
                    .collect(Collectors.toList());
            dto.setGroups(groupDtos);
        }

        return dto;
    }

    public static TrainingForAddGroupDto convertToTrainingForAddGroupDto(Training training) {
        TrainingForAddGroupDto dto = new TrainingForAddGroupDto();
        dto.setId(training.getId());

        if (training.getSiteIds() != null && !training.getSiteIds().isEmpty() && training.getSiteNames() != null && !training.getSiteNames().isEmpty() && training.getSiteIds().size() == training.getSiteNames().size()) {
            List<SiteDto> siteDtos = training.getSiteIds().stream()
                    .map(id -> {
                        int index = training.getSiteIds().indexOf(id);
                        SiteDto siteDto = new SiteDto();
                        siteDto.setId(id);
                        siteDto.setLabel(training.getSiteNames().get(index));
                        return siteDto;
                    })
                    .collect(Collectors.toList());
            dto.setSite(siteDtos);
        }

        if (training.getDepartmentIds() != null && !training.getDepartmentIds().isEmpty() && training.getDepartmentNames() != null && !training.getDepartmentNames().isEmpty() && training.getDepartmentIds().size() == training.getDepartmentNames().size()) {
            List<DepartmentDto> departmentDtos = training.getDepartmentIds().stream()
                    .map(id -> {
                        int index = training.getDepartmentIds().indexOf(id);
                        DepartmentDto departmentDto = new DepartmentDto();
                        departmentDto.setId(id);
                        departmentDto.setName(training.getDepartmentNames().get(index));
                        return departmentDto;
                    })
                    .collect(Collectors.toList());
            dto.setDepartment(departmentDtos);
        }

        return dto;
    }

    public static GetTrainingToEditDto convertToGetTrainingToEditDto(Training training) {
        GetTrainingToEditDto dto = new GetTrainingToEditDto();
        dto.setId(training.getId());

        StrategicAxeDto strategicAxeDto = new StrategicAxeDto();
        strategicAxeDto.setId(training.getStrategicAxeId());
        strategicAxeDto.setTitle(training.getStrategicAxeName());
        dto.setAxe(strategicAxeDto);

        if (training.getSiteIds() != null && !training.getSiteIds().isEmpty() && training.getSiteNames() != null && !training.getSiteNames().isEmpty() && training.getSiteIds().size() == training.getSiteNames().size()) {
            List<SiteDto> siteDtos = training.getSiteIds().stream()
                    .map(id -> {
                        int index = training.getSiteIds().indexOf(id);
                        SiteDto siteDto = new SiteDto();
                        siteDto.setId(id);
                        siteDto.setLabel(training.getSiteNames().get(index));
                        return siteDto;
                    })
                    .collect(Collectors.toList());
            dto.setSite(siteDtos);
        }

        if (training.getDepartmentIds() != null && !training.getDepartmentIds().isEmpty() && training.getDepartmentNames() != null && !training.getDepartmentNames().isEmpty() && training.getDepartmentIds().size() == training.getDepartmentNames().size()) {
            List<DepartmentDto> departmentDtos = training.getDepartmentIds().stream()
                    .map(id -> {
                        int index = training.getDepartmentIds().indexOf(id);
                        DepartmentDto departmentDto = new DepartmentDto();
                        departmentDto.setId(id);
                        departmentDto.setName(training.getDepartmentNames().get(index));
                        return departmentDto;
                    })
                    .collect(Collectors.toList());
            dto.setDepartment(departmentDtos);
        }

        if (training.getDomainId() != null) {
            DomainDto domainDto = new DomainDto();
            domainDto.setId(training.getDomainId());
            domainDto.setName(training.getDomainName());
            dto.setDomain(domainDto);
        }

        if (training.getQualificationId() != null) {
            QualificationDto qualificationDto = new QualificationDto();
            qualificationDto.setId(training.getQualificationId());
            qualificationDto.setType(training.getQualificationName());
            dto.setQualification(qualificationDto);
        }

        dto.setTheme(training.getTheme());
        dto.setNbrDay(training.getNumberOfDay());
        dto.setType(training.getType());
        dto.setNbrGroup(training.getNumberOfGroup());
        dto.setObjective(training.getObjective());
        dto.setContent(training.getContent());
        dto.setCsf(training.getCsf());
        dto.setCsfPlanifie(training.getCsfPlanifie());

        return dto;
    }

    public static void updateTrainingFromTrainingToEditDto(Training trainingToUpdate, EditTrainingDto updateDto) {
        if (updateDto.getAxe() != null) {
            trainingToUpdate.setStrategicAxeId(updateDto.getAxe().getId());
            trainingToUpdate.setStrategicAxeName(updateDto.getAxe().getTitle());
        }
        if (updateDto.getSite() != null) {
            trainingToUpdate.setSiteIds(updateDto.getSite().stream().map(SiteDto::getId).collect(Collectors.toList()));
            trainingToUpdate.setSiteNames(updateDto.getSite().stream().map(SiteDto::getLabel).collect(Collectors.toList()));
        }
        if (updateDto.getDepartment() != null) {
            trainingToUpdate.setDepartmentIds(updateDto.getDepartment().stream().map(DepartmentDto::getId).collect(Collectors.toList()));
            trainingToUpdate.setDepartmentNames(updateDto.getDepartment().stream().map(DepartmentDto::getName).collect(Collectors.toList()));
        }
        if (updateDto.getDomain() != null) {
            trainingToUpdate.setDomainId(updateDto.getDomain().getId());
            trainingToUpdate.setDomainName(updateDto.getDomain().getName());
        }
        if (updateDto.getQualification() != null) {
            trainingToUpdate.setQualificationId(updateDto.getQualification().getId());
            trainingToUpdate.setQualificationName(updateDto.getQualification().getType());
        }
        if (updateDto.getTheme() != null) {
            trainingToUpdate.setTheme(updateDto.getTheme());
        }
        trainingToUpdate.setNumberOfDay(updateDto.getNbrDay());
        if (updateDto.getType() != null) {
            trainingToUpdate.setType(updateDto.getType());
        }
        trainingToUpdate.setNumberOfGroup(updateDto.getNbrGroup());
        if (updateDto.getObjective() != null) {
            trainingToUpdate.setObjective(updateDto.getObjective());
        }
        if (updateDto.getContent() != null) {
            trainingToUpdate.setContent(updateDto.getContent());
        }
        if (updateDto.getCsf() != null) {
            trainingToUpdate.setCsf(updateDto.getCsf());
        }
        if (updateDto.getCsfPlanifie() != null) {
            trainingToUpdate.setCsfPlanifie(updateDto.getCsfPlanifie());
        }
    }

    public static TrainingDetailsForCancelDto convertToTrainingDetailsForCancelDto(Training training) {
        return TrainingDetailsForCancelDto.builder()
                .id(training.getId())
                .theme(training.getTheme())
                .build();
    }
}