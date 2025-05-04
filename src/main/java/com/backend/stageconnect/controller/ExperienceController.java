package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.ExperienceDTO;
import com.backend.stageconnect.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates/{candidateId}/experiences")
@RequiredArgsConstructor
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}
)
public class ExperienceController {
    private static final Logger logger = LoggerFactory.getLogger(ExperienceController.class);
    private final ExperienceService experienceService;

    @GetMapping
    public ResponseEntity<List<ExperienceDTO>> getExperiences(@PathVariable Long candidateId) {
        try {
            logger.info("Fetching experiences for candidate ID: {}", candidateId);
            List<ExperienceDTO> experiences = experienceService.getExperiencesByCandidateId(candidateId);
            return ResponseEntity.ok(experiences);
        } catch (Exception e) {
            logger.error("Error fetching experiences for candidate ID {}: {}", candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<ExperienceDTO> createExperience(
            @PathVariable Long candidateId,
            @RequestBody ExperienceDTO experienceDTO) {
        try {
            logger.info("Creating experience for candidate ID: {}", candidateId);
            logger.debug("Experience data: {}", experienceDTO);
            ExperienceDTO createdExperience = experienceService.createExperience(candidateId, experienceDTO);
            return ResponseEntity.ok(createdExperience);
        } catch (Exception e) {
            logger.error("Error creating experience for candidate ID {}: {}", candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{experienceId}")
    public ResponseEntity<ExperienceDTO> updateExperience(
            @PathVariable Long candidateId,
            @PathVariable Long experienceId,
            @RequestBody ExperienceDTO experienceDTO) {
        try {
            logger.info("Updating experience ID {} for candidate ID: {}", experienceId, candidateId);
            logger.debug("Updated experience data: {}", experienceDTO);
            ExperienceDTO updatedExperience = experienceService.updateExperience(candidateId, experienceId, experienceDTO);
            return ResponseEntity.ok(updatedExperience);
        } catch (Exception e) {
            logger.error("Error updating experience ID {} for candidate ID {}: {}", experienceId, candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{experienceId}")
    public ResponseEntity<Void> deleteExperience(
            @PathVariable Long candidateId,
            @PathVariable Long experienceId) {
        try {
            logger.info("Deleting experience ID {} for candidate ID: {}", experienceId, candidateId);
            experienceService.deleteExperience(candidateId, experienceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting experience ID {} for candidate ID {}: {}", experienceId, candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 