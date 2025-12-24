package com.ray.aiapp.service;

import com.ray.aiapp.service.dto.RagChatResponse;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RagChatService {

    private final OpenAiChatModel chatModel;
    private final ContentRetriever contentRetriever;
    private final ConcurrentHashMap<String, RagAssistant> assistants = new ConcurrentHashMap<>();

    public RagChatService(OpenAiChatModel chatModel, ContentRetriever contentRetriever) {
        this.chatModel = chatModel;
        this.contentRetriever = contentRetriever;
    }

    public RagChatResponse chat(String sessionId, String userMessage) {
        RagAssistant assistant = assistants.computeIfAbsent(sessionId, this::createRagAssistant);
        String response = assistant.chat(userMessage);
        log.debug("RAG Session {}: user='{}', assistant='{}'", sessionId, userMessage, response);
        return new RagChatResponse(sessionId, response);
    }

    public void clearMemory(String sessionId) {
        assistants.remove(sessionId);
        log.info("Cleared RAG memory for session {}", sessionId);
    }

    private RagAssistant createRagAssistant(String sessionId) {
        ChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(40)
                .build();

        return AiServices.builder(RagAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(memory)
                .contentRetriever(contentRetriever)
                .build();
    }

    interface RagAssistant {
        @SystemMessage("""
            You are a helpful assistant named Ray's agent.
            When answering questions, use the provided context from documents when available.
            If you use information from the context, mention that you're referencing the uploaded documents.
            If the context doesn't contain relevant information, answer based on your general knowledge.
            Be concise and friendly.
            """)
        String chat(String userMessage);
    }
}