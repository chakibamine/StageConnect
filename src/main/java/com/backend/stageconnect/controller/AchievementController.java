package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.AchievementDTO;
import com.backend.stageconnect.entity.Achievement;
import com.backend.stageconnect.entity.Company;
import com.backend.stageconnect.repository.AchievementRepository;
import com.backend.stageconnect.repository.CompanyRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies/{companyId}/achievements")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}
)
public class AchievementController {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping
    public ResponseEntity<List<AchievementDTO>> getAllAchievements(@PathVariable Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        List<Achievement> achievements = achievementRepository.findByCompanyId(companyId);
        List<AchievementDTO> achievementDTOs = achievements.stream()
            .map(AchievementDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(achievementDTOs);
    }

    @GetMapping("/{achievementId}")
    public ResponseEntity<AchievementDTO> getAchievement(
            @PathVariable Long companyId,
            @PathVariable Long achievementId) {
        return achievementRepository.findById(achievementId)
                .map(achievement -> {
                    if (!achievement.getCompany().getId().equals(companyId)) {
                        return ResponseEntity.notFound().<AchievementDTO>build();
                    }
                    return ResponseEntity.ok(AchievementDTO.fromEntity(achievement));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createAchievement(
            @PathVariable Long companyId,
            @Valid @RequestBody Achievement achievement,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                    ));
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Validation failed",
                    "errors", errors
                ));
            }

            return companyRepository.findById(companyId)
                    .map(company -> {
                        try {
                            achievement.setCompany(company);
                            Achievement saved = achievementRepository.save(achievement);
                            
                            return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Achievement created successfully",
                                "data", AchievementDTO.fromEntity(saved)
                            ));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create achievement: " + e.getMessage());
                        }
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create achievement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{achievementId}")
    public ResponseEntity<?> updateAchievement(
            @PathVariable Long companyId,
            @PathVariable Long achievementId,
            @Valid @RequestBody Achievement achievementDetails,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                    ));
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Validation failed",
                    "errors", errors
                ));
            }

            if (!companyRepository.existsById(companyId)) {
                return ResponseEntity.notFound().build();
            }

            return achievementRepository.findById(achievementId)
                    .map(achievement -> {
                        if (!achievement.getCompany().getId().equals(companyId)) {
                            return ResponseEntity.notFound().build();
                        }
                        achievement.setTitle(achievementDetails.getTitle());
                        achievement.setDescription(achievementDetails.getDescription());
                        achievement.setIcon(achievementDetails.getIcon());
                        
                        Achievement updated = achievementRepository.save(achievement);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Achievement updated successfully",
                            "data", AchievementDTO.fromEntity(updated)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update achievement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{achievementId}")
    public ResponseEntity<?> deleteAchievement(
            @PathVariable Long companyId,
            @PathVariable Long achievementId) {
        try {
            return achievementRepository.findById(achievementId)
                    .map(achievement -> {
                        if (!achievement.getCompany().getId().equals(companyId)) {
                            return ResponseEntity.notFound().build();
                        }
                        achievementRepository.delete(achievement);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Achievement deleted successfully"
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete achievement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 