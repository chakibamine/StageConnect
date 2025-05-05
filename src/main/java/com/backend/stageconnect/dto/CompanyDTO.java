package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Company;
import lombok.Data;
import java.util.List;

@Data
public class CompanyDTO {
    private Long id;
    private String name;
    private String industry;
    private String size;
    private String foundedDate;
    private String website;
    private String location;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String photo;
    private List<String> technologies;
    private String registrationNumber;
    private String vatId;
    private String legalForm;
    private String linkedInUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String facebookUrl;
    private List<ResponsibleDTO> responsibles;

    public static CompanyDTO fromEntity(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setIndustry(company.getIndustry());
        dto.setSize(company.getSize());
        dto.setFoundedDate(company.getFoundedDate() != null ? company.getFoundedDate().toString() : null);
        dto.setWebsite(company.getWebsite());
        dto.setLocation(company.getLocation());
        dto.setEmail(company.getEmail());
        dto.setPhone(company.getPhone());
        dto.setAddress(company.getAddress());
        dto.setCity(company.getCity());
        dto.setPostalCode(company.getPostalCode());
        dto.setCountry(company.getCountry());
        dto.setPhoto(company.getPhoto());
        dto.setTechnologies(company.getTechnologies());
        dto.setRegistrationNumber(company.getRegistrationNumber());
        dto.setVatId(company.getVatId());
        dto.setLegalForm(company.getLegalForm());
        dto.setLinkedInUrl(company.getLinkedInUrl());
        dto.setTwitterUrl(company.getTwitterUrl());
        dto.setInstagramUrl(company.getInstagramUrl());
        dto.setFacebookUrl(company.getFacebookUrl());
        
        // Convert responsibles to DTOs
        if (company.getResponsibles() != null) {
            dto.setResponsibles(company.getResponsibles().stream()
                .map(ResponsibleDTO::fromEntity)
                .toList());
        }
        
        return dto;
    }
} 