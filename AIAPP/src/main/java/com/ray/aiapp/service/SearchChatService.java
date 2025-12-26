package com.ray.aiapp.service;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        // Inject current date context so AI knows what "today" means
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        String contextualMessage = String.format("[Current date: %s]\n%s", currentDate, userMessage);

        String response = assistant.chat(contextualMessage);
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

        return AiServices.builder(SearchChatAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(memory)
                .tools(webSearchTool)
                .build();
    }

    interface SearchChatAssistant {
        @SystemMessage("You are a helpful AI assistant with access to real-time web search. " +
                      "The current date will be provided at the start of each user message - pay attention to it when they ask about 'today', 'now', or current events. " +
                      "When users ask about current events, recent information, weather, " +
                      "stock prices, or anything that requires up-to-date data, " +
                      "use the web search tool to find accurate information. " +
                      "Always cite your sources when using web search results. " +
                      "Your name is Ray's Search Agent.")
        String chat(String userMessage);
    }
}
