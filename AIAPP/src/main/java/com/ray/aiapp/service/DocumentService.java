package com.ray.aiapp.service;

import com.ray.aiapp.config.properties.LangchainModelProperties;
import com.ray.aiapp.domain.model.Document;
import com.ray.aiapp.domain.model.Document.DocumentStatus;
import com.ray.aiapp.domain.model.Document.DocumentType;
import com.ray.aiapp.exception.ResourceNotFoundException;
import com.ray.aiapp.repository.DocumentRepository;
import com.ray.aiapp.service.dto.DocumentResponse;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final LangchainModelProperties properties;

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, String description) {
        String originalFileName = file.getOriginalFilename();
        DocumentType documentType = determineDocumentType(originalFileName);

        Document document = Document.builder()
                .fileName(UUID.randomUUID().toString())
                .originalFileName(originalFileName)
                .documentType(documentType)
                .fileSize(file.getSize())
                .description(description)
                .status(DocumentStatus.PROCESSING)
                .chunkCount(0)
                .build();

        document = documentRepository.save(document);

        try {
            int chunkCount = processAndIngestDocument(file, document.getId().toString(), documentType);
            document.setChunkCount(chunkCount);
            document.setStatus(DocumentStatus.INDEXED);
            log.info("Document {} indexed successfully with {} chunks", originalFileName, chunkCount);
        } catch (Exception e) {
            log.error("Failed to process document {}: {}", originalFileName, e.getMessage());
            document.setStatus(DocumentStatus.FAILED);
        }

        return DocumentResponse.from(documentRepository.save(document));
    }

    private int processAndIngestDocument(MultipartFile file, String documentId, DocumentType type) throws IOException {
        DocumentParser parser = getParser(type);

        try (InputStream inputStream = file.getInputStream()) {
            dev.langchain4j.data.document.Document langchainDoc = parser.parse(inputStream);

            langchainDoc.metadata().put("documentId", documentId);
            langchainDoc.metadata().put("fileName", file.getOriginalFilename());

            // Split the document first to get actual chunk count
            DocumentSplitter splitter = DocumentSplitters.recursive(
                    properties.getRag().getChunkSize(),
                    properties.getRag().getChunkOverlap());

            List<TextSegment> segments = splitter.split(langchainDoc);

            // Manually embed and store each segment to get exact count
            for (TextSegment segment : segments) {
                embeddingStore.add(embeddingModel.embed(segment).content(), segment);
            }

            // Return the ACTUAL chunk count
            return segments.size();
        }
    }

    private DocumentParser getParser(DocumentType type) {
        return switch (type) {
            case PDF -> new ApachePdfBoxDocumentParser();
            case TEXT, MARKDOWN -> new TextDocumentParser();
        };
    }

    private DocumentType determineDocumentType(String fileName) {
        if (fileName == null) {
            return DocumentType.TEXT;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return DocumentType.PDF;
        } else if (lower.endsWith(".md")) {
            return DocumentType.MARKDOWN;
        }
        return DocumentType.TEXT;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAll().stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .map(DocumentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }
        documentRepository.deleteById(documentId);
        log.info("Document {} deleted from database", documentId);
    }
}