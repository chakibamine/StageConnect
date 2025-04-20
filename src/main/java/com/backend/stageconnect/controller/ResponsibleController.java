package com.backend.stageconnect.controller;

import com.backend.stageconnect.entity.Company;
import com.backend.stageconnect.entity.Responsible;
import com.backend.stageconnect.entity.UserType;
import com.backend.stageconnect.repository.CompanyRepository;
import com.backend.stageconnect.repository.ResponsibleRepository;
import com.backend.stageconnect.repository.UserRepository;
import com.backend.stageconnect.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/responsibles")
@CrossOrigin(origins = "*")
public class ResponsibleController {

    @Autowired
    private ResponsibleRepository responsibleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract data from request
            String firstName = (String) request.get("firstName");
            String lastName = (String) request.get("lastName");
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            Long companyId = Long.valueOf(request.get("companyId").toString());
            
            // Validate input
            if (firstName == null || lastName == null || email == null || password == null || companyId == null) {
                response.put("success", false);
                response.put("message", "All fields are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(email)) {
                response.put("success", false);
                response.put("message", "Email already in use");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if company exists
            Optional<Company> companyOpt = companyRepository.findById(companyId);
            if (companyOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Company not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Company company = companyOpt.get();
            
            // Create responsible entity
            Responsible responsible = Responsible.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .userType(UserType.RESPONSIBLE)
                    .company(company)
                    .enabled(true)
                    .build();
            
            // Save responsible
            Responsible savedResponsible = responsibleRepository.save(responsible);
            
            // Generate JWT token
            String jwtToken = jwtService.generateToken(savedResponsible);
            
            response.put("success", true);
            response.put("message", "Responsible registered successfully");
            response.put("token", jwtToken);
            response.put("id", savedResponsible.getId());
            response.put("firstName", savedResponsible.getFirstName());
            response.put("lastName", savedResponsible.getLastName());
            response.put("email", savedResponsible.getEmail());
            response.put("userType", savedResponsible.getUserType());
            response.put("companyId", company.getId());
            response.put("companyName", company.getName());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Find responsible by email
            Optional<Responsible> responsibleOpt = responsibleRepository.findByEmail(email);
            if (responsibleOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Responsible responsible = responsibleOpt.get();
            
            // Check password
            if (!passwordEncoder.matches(password, responsible.getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Generate JWT token
            String jwtToken = jwtService.generateToken(responsible);
            
            // Successful login
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", jwtToken);
            response.put("id", responsible.getId());
            response.put("firstName", responsible.getFirstName());
            response.put("lastName", responsible.getLastName());
            response.put("email", responsible.getEmail());
            response.put("userType", responsible.getUserType());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getResponsibleById(@PathVariable Long id) {
        Optional<Responsible> responsibleOpt = responsibleRepository.findById(id);
        if (responsibleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Responsible responsible = responsibleOpt.get();
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", responsible.getId());
        response.put("firstName", responsible.getFirstName());
        response.put("lastName", responsible.getLastName());
        response.put("email", responsible.getEmail());
        response.put("companyId", responsible.getCompany().getId());
        response.put("companyName", responsible.getCompany().getName());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/company/{companyId}")
    public ResponseEntity<Map<String, Object>> getResponsiblesByCompany(@PathVariable Long companyId) {
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        if (companyOpt.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Company not found");
            return ResponseEntity.notFound().build();
        }
        
        // Find responsibles by company
        Map<String, Object> response = new HashMap<>();
        response.put("responsibles", responsibleRepository.findByCompanyId(companyId));
        return ResponseEntity.ok(response);
    }
} 