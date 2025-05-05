package com.backend.stageconnect.controller;

import com.backend.stageconnect.dto.CompanyDTO;
import com.backend.stageconnect.entity.Company;
import com.backend.stageconnect.repository.CompanyRepository;
import com.backend.stageconnect.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<?> getCompanyById(@PathVariable Long id) {
        try {
            Optional<Company> companyOpt = companyRepository.findById(id);
            if (companyOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Company company = companyOpt.get();
            CompanyDTO companyDTO = CompanyDTO.fromEntity(company);
            
            return ResponseEntity.ok(companyDTO);
        } catch (Exception e) {
            logger.error("Error processing company data: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing company data: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCompanies() {
        var companies = companyRepository.findAll();
        var response = companies.stream().map(company -> {
            Map<String, Object> companyMap = new HashMap<>();
            
            companyMap.put("id", company.getId());
            companyMap.put("name", company.getName());
            companyMap.put("industry", company.getIndustry());
            companyMap.put("size", company.getSize());
            companyMap.put("foundedDate", company.getFoundedDate());
            companyMap.put("website", company.getWebsite());
            companyMap.put("location", company.getLocation());
            companyMap.put("email", company.getEmail());
            companyMap.put("phone", company.getPhone());
            companyMap.put("address", company.getAddress());
            companyMap.put("city", company.getCity());
            companyMap.put("postalCode", company.getPostalCode());
            companyMap.put("country", company.getCountry());
            companyMap.put("technologies", company.getTechnologies());
            companyMap.put("registrationNumber", company.getRegistrationNumber());
            companyMap.put("vatId", company.getVatId());
            companyMap.put("legalForm", company.getLegalForm());
            companyMap.put("linkedInUrl", company.getLinkedInUrl());
            companyMap.put("twitterUrl", company.getTwitterUrl());
            companyMap.put("instagramUrl", company.getInstagramUrl());
            companyMap.put("facebookUrl", company.getFacebookUrl());
            
            // Add complete photo URL if photo exists
            if (company.getPhoto() != null) {
                companyMap.put("photo", baseUrl + company.getPhoto());
            }
            
            return companyMap;
        }).toList();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createCompany(@RequestBody Company company) {
        try {
            Company savedCompany = companyRepository.save(company);
            return ResponseEntity.status(HttpStatus.CREATED).body(CompanyDTO.fromEntity(savedCompany));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating company: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "data", required = false) Company updatedCompany,
            @RequestBody(required = false) Company jsonCompany) {
        
        Optional<Company> companyOpt = companyRepository.findById(id);
        if (companyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Company company = companyOpt.get();
        Company updateData = updatedCompany != null ? updatedCompany : jsonCompany;
        
        if (updateData == null) {
            return ResponseEntity.badRequest().body("No update data provided");
        }
        
        // Update fields
        company.setName(updateData.getName());
        company.setIndustry(updateData.getIndustry());
        company.setSize(updateData.getSize());
        company.setFoundedDate(updateData.getFoundedDate());
        company.setWebsite(updateData.getWebsite());
        company.setLocation(updateData.getLocation());
        company.setEmail(updateData.getEmail());
        company.setPhone(updateData.getPhone());
        company.setAddress(updateData.getAddress());
        company.setCity(updateData.getCity());
        company.setPostalCode(updateData.getPostalCode());
        company.setCountry(updateData.getCountry());
        company.setTechnologies(updateData.getTechnologies());
        company.setRegistrationNumber(updateData.getRegistrationNumber());
        company.setVatId(updateData.getVatId());
        company.setLegalForm(updateData.getLegalForm());
        company.setLinkedInUrl(updateData.getLinkedInUrl());
        company.setTwitterUrl(updateData.getTwitterUrl());
        company.setInstagramUrl(updateData.getInstagramUrl());
        company.setFacebookUrl(updateData.getFacebookUrl());
        
        // Handle file upload if provided
        if (file != null && !file.isEmpty()) {
            try {
                // Delete old file if exists
                if (company.getPhoto() != null) {
                    fileStorageService.deleteFile(company.getPhoto());
                }
                // Save new file
                String filePath = fileStorageService.saveFile(file);
                company.setPhoto(filePath);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
            }
        }
        
        // Save and return updated company
        Company saved = companyRepository.save(company);
        return ResponseEntity.ok(CompanyDTO.fromEntity(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        Optional<Company> companyOpt = companyRepository.findById(id);
        if (companyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Company company = companyOpt.get();
        
        // Delete photo file if exists
        if (company.getPhoto() != null) {
            try {
                fileStorageService.deleteFile(company.getPhoto());
            } catch (IOException e) {
                // Log error but continue with deletion
                System.err.println("Failed to delete photo file: " + e.getMessage());
            }
        }

        companyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 