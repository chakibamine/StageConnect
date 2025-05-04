package com.backend.stageconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
@DiscriminatorValue("CANDIDATE")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate extends User {
    
    @Column(name = "photo")
    private String photo;

    private String phone;
    private String location;
    private String title;
    private String website;
    private String companyOrUniversity;
    
    @Column(length = 2000)
    private String about;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> education = new ArrayList<>();

    // Helper method to add education
    public void addEducation(Education education) {
        this.education.add(education);
        education.setCandidate(this);
    }

    // Helper method to remove education
    public void removeEducation(Education education) {
        this.education.remove(education);
        education.setCandidate(null);
    }
} 