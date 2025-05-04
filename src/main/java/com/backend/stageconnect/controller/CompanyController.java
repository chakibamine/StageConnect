package com.backend.stageconnect.controller;

import com.backend.stageconnect.entity.Company;
import com.backend.stageconnect.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyRepository companyRepository;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCompany(@RequestBody Company company) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if email already exists
            if (company.getEmail() != null && companyRepository.existsByEmail(company.getEmail())) {
                response.put("success", false);
                response.put("message", "Company with this email already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if registration number already exists
            if (company.getRegistrationNumber() != null && 
                companyRepository.existsByRegistrationNumber(company.getRegistrationNumber())) {
                response.put("success", false);
                response.put("message", "Company with this registration number already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save company
            Company savedCompany = companyRepository.save(company);
            
            response.put("success", true);
            response.put("message", "Company created successfully");
            response.put("id", savedCompany.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create company: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return ResponseEntity.ok(companies);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        Optional<Company> companyOpt = companyRepository.findById(id);
        if (companyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(companyOpt.get());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCompany(@PathVariable Long id, @RequestBody Company companyDetails) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Company> companyOpt = companyRepository.findById(id);
            if (companyOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Company not found");
                return ResponseEntity.notFound().build();
            }
            
            Company company = companyOpt.get();
            
            // Check if email is being changed and if the new email is already in use
            if (companyDetails.getEmail() != null && !companyDetails.getEmail().equals(company.getEmail()) && 
                companyRepository.existsByEmail(companyDetails.getEmail())) {
                response.put("success", false);
                response.put("message", "Email already in use by another company");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if registration number is being changed and if the new one is already in use
            if (companyDetails.getRegistrationNumber() != null && 
                !companyDetails.getRegistrationNumber().equals(company.getRegistrationNumber()) && 
                companyRepository.existsByRegistrationNumber(companyDetails.getRegistrationNumber())) {
                response.put("success", false);
                response.put("message", "Registration number already in use by another company");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Update fields from request
            if (companyDetails.getName() != null) company.setName(companyDetails.getName());
            if (companyDetails.getIndustry() != null) company.setIndustry(companyDetails.getIndustry());
            if (companyDetails.getSize() != null) company.setSize(companyDetails.getSize());
            if (companyDetails.getFoundedDate() != null) company.setFoundedDate(companyDetails.getFoundedDate());
            if (companyDetails.getWebsite() != null) company.setWebsite(companyDetails.getWebsite());
            if (companyDetails.getLocation() != null) company.setLocation(companyDetails.getLocation());
            if (companyDetails.getEmail() != null) company.setEmail(companyDetails.getEmail());
            if (companyDetails.getPhone() != null) company.setPhone(companyDetails.getPhone());
            if (companyDetails.getAddress() != null) company.setAddress(companyDetails.getAddress());
            if (companyDetails.getCity() != null) company.setCity(companyDetails.getCity());
            if (companyDetails.getPostalCode() != null) company.setPostalCode(companyDetails.getPostalCode());
            if (companyDetails.getCountry() != null) company.setCountry(companyDetails.getCountry());
            if (companyDetails.getPhoto() != null) company.setPhoto(companyDetails.getPhoto());
            if (companyDetails.getTechnologies() != null) {
                company.getTechnologies().clear();
                company.getTechnologies().addAll(companyDetails.getTechnologies());
            }
            if (companyDetails.getRegistrationNumber() != null) company.setRegistrationNumber(companyDetails.getRegistrationNumber());
            if (companyDetails.getVatId() != null) company.setVatId(companyDetails.getVatId());
            if (companyDetails.getLegalForm() != null) company.setLegalForm(companyDetails.getLegalForm());
            if (companyDetails.getLinkedInUrl() != null) company.setLinkedInUrl(companyDetails.getLinkedInUrl());
            if (companyDetails.getTwitterUrl() != null) company.setTwitterUrl(companyDetails.getTwitterUrl());
            if (companyDetails.getInstagramUrl() != null) company.setInstagramUrl(companyDetails.getInstagramUrl());
            if (companyDetails.getFacebookUrl() != null) company.setFacebookUrl(companyDetails.getFacebookUrl());
            
            // Save updated company
            Company updatedCompany = companyRepository.save(company);
            
            response.put("success", true);
            response.put("message", "Company updated successfully");
            response.put("id", updatedCompany.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update company: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCompany(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Company> companyOpt = companyRepository.findById(id);
            if (companyOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Company not found");
                return ResponseEntity.notFound().build();
            }
            
            companyRepository.deleteById(id);
            
            response.put("success", true);
            response.put("message", "Company deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete company: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 