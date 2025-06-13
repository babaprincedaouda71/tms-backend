package org.example.trainingservice.service.ocf;

import org.springframework.http.ResponseEntity;

public interface OCFService {
    ResponseEntity<?> getAllOcf();

    ResponseEntity<?> getOcfAddOrEditGroup();
}