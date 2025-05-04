package com.backend.stageconnect.service;

import com.backend.stageconnect.dto.ExperienceDTO;
import com.backend.stageconnect.entity.Candidate;
import com.backend.stageconnect.entity.Experience;
import com.backend.stageconnect.repository.CandidateRepository;
import com.backend.stageconnect.repository.ExperienceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExperienceService {
    private final ExperienceRepository experienceRepository;
    private final CandidateRepository candidateRepository;

    public List<ExperienceDTO> getExperiencesByCandidateId(Long candidateId) {
        return experienceRepository.findByCandidateId(candidateId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExperienceDTO createExperience(Long candidateId, ExperienceDTO experienceDTO) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        Experience experience = new Experience();
        updateExperienceFromDTO(experience, experienceDTO);
        experience.setCandidate(candidate);

        Experience savedExperience = experienceRepository.save(experience);
        return convertToDTO(savedExperience);
    }

    @Transactional
    public ExperienceDTO updateExperience(Long candidateId, Long experienceId, ExperienceDTO experienceDTO) {
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new EntityNotFoundException("Experience not found"));

        if (!experience.getCandidate().getId().equals(candidateId)) {
            throw new IllegalArgumentException("Experience does not belong to the specified candidate");
        }

        updateExperienceFromDTO(experience, experienceDTO);
        Experience updatedExperience = experienceRepository.save(experience);
        return convertToDTO(updatedExperience);
    }

    @Transactional
    public void deleteExperience(Long candidateId, Long experienceId) {
        experienceRepository.deleteByCandidateIdAndId(candidateId, experienceId);
    }

    private ExperienceDTO convertToDTO(Experience experience) {
        ExperienceDTO dto = new ExperienceDTO();
        dto.setId(experience.getId());
        dto.setTitle(experience.getTitle());
        dto.setCompany(experience.getCompany());
        dto.setLocation(experience.getLocation());
        dto.setStartDate(experience.getStartDate());
        dto.setEndDate(experience.getEndDate());
        dto.setDescription(experience.getDescription());
        dto.setCandidateId(experience.getCandidate().getId());
        return dto;
    }

    private void updateExperienceFromDTO(Experience experience, ExperienceDTO dto) {
        experience.setTitle(dto.getTitle());
        experience.setCompany(dto.getCompany());
        experience.setLocation(dto.getLocation());
        experience.setStartDate(dto.getStartDate());
        experience.setEndDate(dto.getEndDate());
        experience.setDescription(dto.getDescription());
    }
} 