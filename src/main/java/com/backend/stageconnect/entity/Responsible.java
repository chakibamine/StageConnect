package com.backend.stageconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "responsibles")
@DiscriminatorValue("RESPONSIBLE")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Responsible extends User {
    
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
} 