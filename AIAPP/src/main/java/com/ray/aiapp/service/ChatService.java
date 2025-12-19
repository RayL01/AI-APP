package com.ray.aiapp.service;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatService {

    private final OpenAiChatModel chatModel;
    private final ConcurrentHashMap<String, ChatAssistant> assistants = new ConcurrentHashMap<>();

    public ChatService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String chat(String sessionId, String userMessage) {
        ChatAssistant assistant = assistants.computeIfAbsent(sessionId, this::createAssistant);
        String response = assistant.chat(userMessage);
        log.debug("Session {}: user='{}', assistant='{}'", sessionId, userMessage, response);
        return response;
    }

    public void clearMemory(String sessionId) {
        assistants.remove(sessionId);
        log.info("Cleared memory for session {}", sessionId);
    }

    private ChatAssistant createAssistant(String sessionId) {
        ChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(40)
                .build();

        return AiServices.builder(ChatAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(memory)
                .build();
    }

    interface ChatAssistant {
        String chat(String userMessage);
    }
}