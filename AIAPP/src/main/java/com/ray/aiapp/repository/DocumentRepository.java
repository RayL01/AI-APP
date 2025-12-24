package com.ray.aiapp.repository;

import com.ray.aiapp.domain.model.Document;
import com.ray.aiapp.domain.model.Document.DocumentStatus;
import com.ray.aiapp.domain.model.Document.DocumentType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByStatus(DocumentStatus status);

    List<Document> findByDocumentType(DocumentType documentType);

    boolean existsByOriginalFileName(String originalFileName);
}