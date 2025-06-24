package org.example.trainingservice.service.plan;

import org.example.trainingservice.dto.plan.AddGroupeInvoiceDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface GroupeInvoiceService {
    ResponseEntity<?> getAll(Long groupId);

    ResponseEntity<?> getGroupeInvoice(UUID groupeInvoiceId);

    ResponseEntity<?> addGroupeInvoice(Long groupId, AddGroupeInvoiceDto invoice, MultipartFile invoiceFile, MultipartFile bankRemiseFile, MultipartFile receiptFile);

    ResponseEntity<?> deleteGroupeInvoice(UUID groupeInvoiceId);
}