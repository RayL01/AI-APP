package com.ray.aiapp.service;

import com.ray.aiapp.domain.model.AssistantProfile;
import com.ray.aiapp.exception.ResourceAlreadyExistsException;
import com.ray.aiapp.exception.ResourceNotFoundException;
import com.ray.aiapp.repository.AssistantProfileRepository;
import com.ray.aiapp.service.dto.AssistantProfileRequest;
import com.ray.aiapp.service.dto.AssistantProfileResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantProfileService {

    private final AssistantProfileRepository repository;

    @Transactional(readOnly = true)
    public List<AssistantProfileResponse> listProfiles() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AssistantProfileResponse createProfile(AssistantProfileRequest request) {
        repository.findByNameIgnoreCase(request.name())
                .ifPresent(profile -> {
                    throw new ResourceAlreadyExistsException("Profile name already in use: " + request.name());
                });

        AssistantProfile profile = new AssistantProfile();
        profile.setName(request.name());
        profile.setDescription(request.description());
        profile.setModel(request.model());
        profile.setTemperature(request.temperature());

        AssistantProfile persisted = repository.save(profile);
        log.info("Created assistant profile {} using model {}", persisted.getName(), persisted.getModel());
        return toResponse(persisted);
    }

    @Transactional
    public void deleteProfile(UUID profileId) {
        AssistantProfile profile = repository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        repository.delete(profile);
        log.warn("Assistant profile {} deleted", profile.getName());
    }

    private AssistantProfileResponse toResponse(AssistantProfile profile) {
        return new AssistantProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getDescription(),
                profile.getModel(),
                profile.getTemperature(),
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }
}
