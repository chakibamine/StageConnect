package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.ProjectDTO;
import com.backend.stageconnect.entity.Project;
import com.backend.stageconnect.entity.Company;
import com.backend.stageconnect.repository.ProjectRepository;
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
import java.util.ArrayList;

@RestController
@RequestMapping("/api/companies/{companyId}/projects")
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"},
    allowCredentials = "true",
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers", "x-cors-debug"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}
)
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects(@PathVariable Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            return ResponseEntity.notFound().build();
        }
        
        List<Project> projects = projectRepository.findByCompanyId(companyId);
        List<ProjectDTO> projectDTOs = projects.stream()
            .map(ProjectDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(projectDTOs);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDTO> getProject(
            @PathVariable Long companyId,
            @PathVariable Long projectId) {
        return projectRepository.findById(projectId)
                .map(project -> {
                    if (!project.getCompany().getId().equals(companyId)) {
                        return ResponseEntity.notFound().<ProjectDTO>build();
                    }
                    return ResponseEntity.ok(ProjectDTO.fromEntity(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProject(
            @PathVariable Long companyId,
            @Valid @RequestBody Project project,
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
                            // Convert tags automatically with setter
                            if (project.getTags() == null) {
                                project.setTags(new ArrayList<>());
                            }
                            
                            project.setCompany(company);
                            Project saved = projectRepository.save(project);
                            
                            return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Project created successfully",
                                "data", ProjectDTO.fromEntity(saved)
                            ));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to create project: " + e.getMessage());
                        }
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create project: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(
            @PathVariable Long companyId,
            @PathVariable Long projectId,
            @Valid @RequestBody Project projectDetails,
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

            return projectRepository.findById(projectId)
                    .map(project -> {
                        if (!project.getCompany().getId().equals(companyId)) {
                            return ResponseEntity.notFound().build();
                        }
                        
                        project.setTitle(projectDetails.getTitle());
                        project.setDescription(projectDetails.getDescription());
                        
                        // Tags are now handled by the entity itself
                        project.setTags(projectDetails.getTags());
                        
                        Project updated = projectRepository.save(project);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Project updated successfully",
                            "data", ProjectDTO.fromEntity(updated)
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update project: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(
            @PathVariable Long companyId,
            @PathVariable Long projectId) {
        try {
            return projectRepository.findById(projectId)
                    .map(project -> {
                        if (!project.getCompany().getId().equals(companyId)) {
                            return ResponseEntity.notFound().build();
                        }
                        projectRepository.delete(project);
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Project deleted successfully"
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete project: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 