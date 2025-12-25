package com.ray.aiapp.service;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat service with web search capability.
 * The AI can autonomously decide when to search the web for current information.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "aiapp.langchain.web-search", name = "enabled", havingValue = "true")
public class SearchChatService {

    private final OpenAiChatModel chatModel;
    private final WebSearchTool webSearchTool;
    private final ConcurrentHashMap<String, SearchChatAssistant> assistants = new ConcurrentHashMap<>();

    public SearchChatService(OpenAiChatModel chatModel, WebSearchTool webSearchTool) {
        this.chatModel = chatModel;
        this.webSearchTool = webSearchTool;
        log.info("SearchChatService initialized with web search capability");
    }

    public String chat(String sessionId, String userMessage) {
        SearchChatAssistant assistant = assistants.computeIfAbsent(sessionId, this::createAssistant);
        String response = assistant.chat(userMessage);
        log.debug("Session {}: user='{}', assistant='{}'", sessionId, userMessage, response);
        return response;
    }

    public void clearMemory(String sessionId) {
        assistants.remove(sessionId);
        log.info("Cleared memory for search chat session {}", sessionId);
    }

    private SearchChatAssistant createAssistant(String sessionId) {
        ChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(40)
                .build();

        // TODO(human): Integrate the WebSearchTool with AiServices
        // Step 1: Use AiServices.builder(SearchChatAssistant.class)
        // Step 2: Add the chatLanguageModel (already available as this.chatModel)
        // Step 3: Add the chatMemory (already created above as 'memory')
        // Step 4: IMPORTANT: Register the webSearchTool using .tools(webSearchTool)
        //         This is the key line that gives the AI access to web search
        // Step 5: Call .build() to create the assistant
        //
        // Hint: This should look very similar to ChatService.createAssistant()
        // but with one extra line: .tools(webSearchTool)
        //
        // The AI will automatically decide when to use the search tool based on
        // the @Tool annotation description in WebSearchTool

        throw new UnsupportedOperationException("TODO: Implement AI service creation with tool integration");
    }

    interface SearchChatAssistant {
        @SystemMessage("You are a helpful AI assistant with access to real-time web search. " +
                      "When users ask about current events, recent information, weather, " +
                      "stock prices, or anything that requires up-to-date data, " +
                      "use the web search tool to find accurate information. " +
                      "Always cite your sources when using web search results. " +
                      "Your name is Ray's Search Agent.")
        String chat(String userMessage);
    }
}
