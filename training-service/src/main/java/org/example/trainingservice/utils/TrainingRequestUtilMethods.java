package org.example.trainingservice.utils;

import org.example.trainingservice.dto.trainingRequest.AddMyTrainingRequestDto;
import org.example.trainingservice.dto.trainingRequest.TeamRequestsDto;
import org.example.trainingservice.entity.Need;
import org.example.trainingservice.entity.TrainingRequest;
import org.example.trainingservice.enums.NeedSource;
import org.example.trainingservice.enums.NeedStatusEnums;
import org.example.trainingservice.enums.TrainingRequestStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrainingRequestUtilMethods {
    public static TrainingRequest mapToTrainingRequest(AddMyTrainingRequestDto newRequest, Long currentCompanyId) {
        return TrainingRequest.builder()
                .companyId(currentCompanyId)
                .year(newRequest.getYear())
                .domain(newRequest.getDomain())
                .site(newRequest.getSite())
                .department(newRequest.getDepartment())
                .theme(newRequest.getTheme())
                .wishDate(newRequest.getWishDate())
                .creationDate(LocalDate.now())
                .requesterId(newRequest.getRequester().getRequesterId())
                .requesterName(newRequest.getRequester().getRequesterFullName())
                .managerId(newRequest.getRequester().getManagerId())
                .managerId(newRequest.getRequester().getManagerId())
                .objective(newRequest.getObjective())
                .learningMode(newRequest.getLearningMode())
                .status(TrainingRequestStatus.Waiting)
                .build();
    }

    public static List<TeamRequestsDto> mapToTeamRequestsDto(List<TrainingRequest> trainingRequests) {
        List<TeamRequestsDto> teamRequestsDtos = new ArrayList<>();
        for (TrainingRequest trainingRequest : trainingRequests) {
            TeamRequestsDto dto = TeamRequestsDto.builder()
                    .id(trainingRequest.getId())
                    .year(trainingRequest.getYear())
                    .domain(trainingRequest.getDomain())
                    .theme(trainingRequest.getTheme())
                    .site(trainingRequest.getSite())
                    .department(trainingRequest.getDepartment())
                    .creationDate(trainingRequest.getCreationDate())
                    .requester(trainingRequest.getRequesterName())
                    .approver(trainingRequest.getApprover())
                    .status(trainingRequest.getStatus().toString())
                    .build();
            teamRequestsDtos.add(dto);
        }
        return teamRequestsDtos;
    }

    public static Need convertTrainingRequestToNeed(TrainingRequest request, Long approverId) {
        return Need.builder()
                .companyId(request.getCompanyId())
                .theme(request.getTheme())
                .objective(request.getObjective())
                .content(request.getContent())
                .year(request.getYear())
                .wishDate(request.getWishDate() != null ? request.getWishDate().toString() : null)
                .requesterId(request.getRequesterId())
                .requesterName(request.getRequesterName())
                .approverId(approverId)
                .learningMode(request.getLearningMode())
                .domainName(request.getDomain())
                .siteNames(List.of(request.getSite()))
                .departmentNames(List.of(request.getDepartment()))
                .status(NeedStatusEnums.DRAFT)  // statut par défaut pour les besoins issus de demandes
                .source(NeedSource.Individual_Requests)  // source indiquant qu'il s'agit d'une demande individuelle
                .creationDate(LocalDate.now().toString())
                // valeurs par défaut pour les champs obligatoires
                .numberOfDay(1)  // à ajuster selon vos besoins
                .numberOfGroup(1)  // à ajuster selon vos besoins
                .build();
    }
}