package org.example.trainingservice.repository.evaluation;

import org.example.trainingservice.entity.campaign.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, UUID> {
    Optional<UserResponse> findByQuestionnaireId(UUID questionnaireId);

    List<UserResponse> findByUserIdAndQuestionnaireId(Long userId, UUID questionnaireId);

    Optional<UserResponse> findByUserIdAndQuestionnaireIdAndQuestionId(Long userId, UUID questionnaireId, UUID questionId);

    List<UserResponse> findByQuestionnaireIdAndUserIdIn(UUID id, List<Long> myTeamIds);

    List<UserResponse> findByQuestionnaireIdInAndUserIdIn(Collection<UUID> questionnaireIds, Collection<Long> userIds);

    List<UserResponse> findByQuestionnaireIdInAndUserIdInAndIsSentToAdmin(Collection<UUID> questionnaireIds, Collection<Long> userIds, Boolean isSentToAdmin);
}