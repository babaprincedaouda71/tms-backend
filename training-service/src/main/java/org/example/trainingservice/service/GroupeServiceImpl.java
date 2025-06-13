package org.example.trainingservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingservice.dto.group.AddOrEditGroupExternalProviderDto;
import org.example.trainingservice.dto.group.AddOrEditGroupInternalProviderDto;
import org.example.trainingservice.dto.group.AddOrEditGroupParticipantsDto;
import org.example.trainingservice.dto.group.AddOrEditGroupPlanningDto;
import org.example.trainingservice.service.groups.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GroupeServiceImpl implements GroupeService {
    private final GroupCreationService groupCreationService;
    private final GroupPlanningService groupPlanningService;
    private final GroupParticipantsService groupParticipantsService;
    private final GroupInternalProviderService groupInternalProviderService;
    private final GroupExternalProviderService groupExternalProviderService;
    private final GroupRetrievalService groupRetrievalService;
    private final GroupDeletionService groupDeletionService;

    public GroupeServiceImpl(
            GroupCreationService groupCreationService,
            GroupPlanningService groupPlanningService,
            GroupParticipantsService groupParticipantsService,
            GroupInternalProviderService groupInternalProviderService,
            GroupExternalProviderService groupExternalProviderService,
            GroupRetrievalService groupRetrievalService, GroupDeletionService groupDeletionService) {
        this.groupCreationService = groupCreationService;
        this.groupPlanningService = groupPlanningService;
        this.groupParticipantsService = groupParticipantsService;
        this.groupInternalProviderService = groupInternalProviderService;
        this.groupExternalProviderService = groupExternalProviderService;
        this.groupRetrievalService = groupRetrievalService;
        this.groupDeletionService = groupDeletionService;
    }

    @Override
    public ResponseEntity<?> duplicateGroup(Long id) {
        return groupCreationService.duplicateGroup(id);
    }

    @Override
    public ResponseEntity<?> addGroupPlanning(Long needId, AddOrEditGroupPlanningDto addOrEditGroupPlanningDto) {
        return groupPlanningService.addGroupPlanning(needId, addOrEditGroupPlanningDto);
    }

    @Override
    public ResponseEntity<?> addGroupParticipants(Long needId, AddOrEditGroupParticipantsDto addOrEditGroupParticipantsDto) {
        return groupParticipantsService.addGroupParticipants(needId, addOrEditGroupParticipantsDto);
    }

    @Override
    public ResponseEntity<?> addGroupInternalProvider(Long needId, AddOrEditGroupInternalProviderDto addOrEditGroupInternalProviderDto) {
        return groupInternalProviderService.addGroupInternalProvider(needId, addOrEditGroupInternalProviderDto);
    }

    @Override
    public ResponseEntity<?> addGroupExternalProvider(Long needId, AddOrEditGroupExternalProviderDto addOrEditGroupExternalProviderDto) {
        return groupExternalProviderService.addGroupExternalProvider(needId, addOrEditGroupExternalProviderDto);
    }

    @Override
    public ResponseEntity<?> editGroupPlanning(Long groupId, AddOrEditGroupPlanningDto addOrEditGroupPlanningDto) {
        return groupPlanningService.editGroupPlanning(groupId, addOrEditGroupPlanningDto);
    }

    @Override
    public ResponseEntity<?> editGroupParticipants(Long groupId, AddOrEditGroupParticipantsDto addOrEditGroupParticipantsDto) {
        return groupParticipantsService.editGroupParticipants(groupId, addOrEditGroupParticipantsDto);
    }

    @Override
    public ResponseEntity<?> editGroupInternalProvider(Long groupId, AddOrEditGroupInternalProviderDto addOrEditGroupInternalProviderDto) {
        return groupInternalProviderService.editGroupInternalProvider(groupId, addOrEditGroupInternalProviderDto);
    }

    @Override
    public ResponseEntity<?> editGroupExternalProvider(Long groupId, AddOrEditGroupExternalProviderDto addOrEditGroupExternalProviderDto) {
        return groupExternalProviderService.editGroupExternalProvider(groupId, addOrEditGroupExternalProviderDto);
    }

    @Override
    public ResponseEntity<?> getGroupToAddOrEdit(Long groupId) {
        return groupRetrievalService.getGroupToAddOrEdit(groupId);
    }

    @Override
    public ResponseEntity<?> deleteGroup(Long groupId) {
        return groupDeletionService.deleteGroup(groupId);
    }
}