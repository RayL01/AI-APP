package com.ray.aiapp.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document extends AbstractAuditableEntity {

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private int chunkCount;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    public enum DocumentType {
        PDF, TEXT, MARKDOWN
    }

    public enum DocumentStatus {
        PROCESSING, INDEXED, FAILED
    }
}