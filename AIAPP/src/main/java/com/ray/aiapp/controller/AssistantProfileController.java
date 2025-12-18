package com.ray.aiapp.controller;

import com.ray.aiapp.service.AssistantProfileService;
import com.ray.aiapp.service.dto.AssistantProfileRequest;
import com.ray.aiapp.service.dto.AssistantProfileResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assistant-profiles")
public class AssistantProfileController {

    private final AssistantProfileService assistantProfileService;

    @GetMapping
    public ResponseEntity<List<AssistantProfileResponse>> findAll() {
        return ResponseEntity.ok(assistantProfileService.listProfiles());
    }

    @PostMapping
    public ResponseEntity<AssistantProfileResponse> createProfile(@Valid @RequestBody AssistantProfileRequest request) {
        AssistantProfileResponse response = assistantProfileService.createProfile(request);
        log.debug("Assistant profile {} created", response.name());
        return ResponseEntity.created(URI.create("/api/v1/assistant-profiles/" + response.id())).body(response);
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID profileId) {
        assistantProfileService.deleteProfile(profileId);
        return ResponseEntity.noContent().build();
    }
}
