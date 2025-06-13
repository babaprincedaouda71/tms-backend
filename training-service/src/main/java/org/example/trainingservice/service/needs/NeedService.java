package org.example.trainingservice.service.needs;

import org.example.trainingservice.dto.need.AddStrategicAxeNeedDto;
import org.example.trainingservice.dto.need.UpdateStatusRequestDto;
import org.springframework.http.ResponseEntity;

public interface NeedService {
    ResponseEntity<?> getAllNeeds();

    ResponseEntity<?> getAllValidatedNeedToAddToPlan();

    ResponseEntity<?> addStrategicAxesNeed(AddStrategicAxeNeedDto addStrategicAxeNeedDto);

    ResponseEntity<?> getAllStrategicAxesNeed();

    ResponseEntity<?> updateStatus(UpdateStatusRequestDto updateStatusRequestDto);

    ResponseEntity<?> delete(Long id);

    ResponseEntity<?> getNeedById(Long id);

    ResponseEntity<?> getNeedForAddGroup(Long id);

    ResponseEntity<?> getDetailsById(Long id);

    ResponseEntity<?> editStrategicAxe(Long id, AddStrategicAxeNeedDto addStrategicAxeNeedDto);

    ResponseEntity<?> getAllIndividualRequestNeeds();

    ResponseEntity<?> getAllEvaluationNeed();
}