package com.ray.aiapp.repository;

import com.ray.aiapp.domain.model.AssistantProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantProfileRepository extends JpaRepository<AssistantProfile, UUID> {

    Optional<AssistantProfile> findByNameIgnoreCase(String name);
}
