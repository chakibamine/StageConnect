package com.backend.stageconnect.dto;

import com.backend.stageconnect.entity.Certification;
import lombok.Data;

@Data
public class CertificationDTO {
    private Long id;
    private String name;
    private String issuer;
    private String date;
    private String credentialId;
    private String url;
    private Long candidateId;

    public static CertificationDTO fromEntity(Certification certification) {
        CertificationDTO dto = new CertificationDTO();
        dto.setId(certification.getId());
        dto.setName(certification.getName());
        dto.setIssuer(certification.getIssuer());
        dto.setDate(certification.getDate());
        dto.setCredentialId(certification.getCredentialId());
        dto.setUrl(certification.getUrl());
        if (certification.getCandidate() != null) {
            dto.setCandidateId(certification.getCandidate().getId());
        }
        return dto;
    }
} 