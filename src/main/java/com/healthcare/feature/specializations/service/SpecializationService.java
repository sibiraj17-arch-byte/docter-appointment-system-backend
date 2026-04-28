package com.healthcare.feature.specializations.service;

import com.healthcare.entity.Specialization;
import com.healthcare.exception.DuplicateResourceException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.feature.specializations.dto.*;
import com.healthcare.feature.specializations.repository.SpecializationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecializationService {

    private final SpecializationRepository specializationRepository;

    public SpecializationService(SpecializationRepository specializationRepository) {
        this.specializationRepository = specializationRepository;
    }

    public List<SpecializationResponseDTO> getAll() {
        return specializationRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public SpecializationResponseDTO getById(Long id) {
        return toDTO(specializationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization", "id", id)));
    }

    public SpecializationResponseDTO create(SpecializationRequestDTO request) {
        if (specializationRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Specialization already exists with name: " + request.getName());
        }
        Specialization spec = new Specialization();
        spec.setName(request.getName());
        spec.setDescription(request.getDescription());
        return toDTO(specializationRepository.save(spec));
    }

    public SpecializationResponseDTO update(Long id, SpecializationRequestDTO request) {
        Specialization spec = specializationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization", "id", id));
        spec.setName(request.getName());
        spec.setDescription(request.getDescription());
        return toDTO(specializationRepository.save(spec));
    }

    public void delete(Long id) {
        Specialization spec = specializationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization", "id", id));
        specializationRepository.delete(spec);
    }

    private SpecializationResponseDTO toDTO(Specialization spec) {
        SpecializationResponseDTO dto = new SpecializationResponseDTO();
        dto.setId(spec.getId());
        dto.setName(spec.getName());
        dto.setDescription(spec.getDescription());
        dto.setCreatedAt(spec.getCreatedAt());
        return dto;
    }
}
