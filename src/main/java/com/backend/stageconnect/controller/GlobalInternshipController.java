package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.InternshipDTO;
import com.backend.stageconnect.entity.Internship;
import com.backend.stageconnect.repository.InternshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/internships")
public class GlobalInternshipController {
    private static final Logger logger = Logger.getLogger(GlobalInternshipController.class.getName());
    
    @Autowired
    private InternshipRepository internshipRepository;

    @GetMapping("/id/{internshipId}")
    public ResponseEntity<?> getInternshipByIdOnly(@PathVariable Long internshipId) {
        logger.info("Fetching internship with ID: " + internshipId);
        
        return internshipRepository.findById(internshipId)
                .map(internship -> {
                    logger.info("Found internship: " + internship.getTitle());
                    if (internship.getCompany() != null) {
                        logger.info("Company: " + internship.getCompany().getName());
                    } else {
                        logger.warning("Company is null for internship ID: " + internshipId);
                    }
                    
                    InternshipDTO dto = InternshipDTO.fromEntity(internship);
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Internship found",
                        "data", dto
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Internship not found"
                )));
    }
} 