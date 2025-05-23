package com.backend.stageconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "internships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Internship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;
    
    @NotBlank(message = "Location is required")
    @Column(nullable = false)
    private String location;
    
    @Column(name = "work_type")
    private String workType;
    
    @Column
    private String duration;
    
    @Column
    private String compensation;
    
    @Column(name = "applicants_count")
    private Integer applicantsCount = 0;
    
    @Column
    private String status = "active";
    
    @Column(name = "posted_date")
    private String postedDate;
    
    @Column(name = "deadline")
    private String deadline;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({"internships", "password", "enabled", "userType"})
    private Company company;
    
    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("internship")
    private List<Application> applications = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    // Helper method to set up bidirectional relationship
    public void setCompany(Company company) {
        this.company = company;
        if (company != null && !company.getInternships().contains(this)) {
            company.getInternships().add(this);
        }
    }
    
    // Helper method to add an application
    public void addApplication(Application application) {
        applications.add(application);
        application.setInternship(this);
        this.applicantsCount = this.applications.size();
    }
    
    // Helper method to remove an application
    public void removeApplication(Application application) {
        applications.remove(application);
        application.setInternship(null);
        this.applicantsCount = this.applications.size();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
} 