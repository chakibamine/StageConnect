package com.backend.stageconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String industry;
    private String size;
    private LocalDate foundedDate;
    private String website;
    private String location;
    
    @Column(unique = true)
    private String email;
    
    private String phone;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    
    private String photo;
    
    @ElementCollection
    @CollectionTable(name = "company_technologies", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "technology")
    private List<String> technologies = new ArrayList<>();
    
    private String registrationNumber;
    private String vatId;
    private String legalForm;
    private String linkedInUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String facebookUrl;
    
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Responsible> responsibles = new ArrayList<>();
} 