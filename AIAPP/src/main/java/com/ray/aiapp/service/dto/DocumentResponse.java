package com.ray.aiapp.service.dto;

import com.ray.aiapp.domain.model.Document;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String fileName,
        String originalFileName,
        String documentType,
        long fileSize,
        int chunkCount,
        String description,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getOriginalFileName(),
                document.getDocumentType().name(),
                document.getFileSize(),
                document.getChunkCount(),
                document.getDescription(),
                document.getStatus().name(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}