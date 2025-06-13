package org.example.trainingservice.web.OCF;

import org.example.trainingservice.service.ocf.OCFService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ocf")
public class OCFController {
    private final OCFService ocfService;

    public OCFController(OCFService ocfService) {
        this.ocfService = ocfService;
    }

    @GetMapping("/get/all")
    public ResponseEntity<?> getAll() {
        return ocfService.getAllOcf();
    }

    @GetMapping("/get/ocfAddOrEditGroup")
    public ResponseEntity<?> getOcfAddOrEditGroup() {
        return ocfService.getOcfAddOrEditGroup();
    }
}