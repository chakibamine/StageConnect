package com.backend.stageconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
} 