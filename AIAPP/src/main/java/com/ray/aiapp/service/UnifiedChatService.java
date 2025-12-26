package com.ray.aiapp.service;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified chat service that combines all AI capabilities:
 * - Basic conversational AI
 * - RAG (Retrieval-Augmented Generation) with uploaded documents
 * - Web search for current information
 *
 * The AI will autonomously decide which capabilities to use based on the user's query.
 */
@Slf4j
@Service
public class UnifiedChatService {

    private final OpenAiChatModel chatModel;
    private final ContentRetriever contentRetriever;
    private final WebSearchTool webSearchTool;
    private final ConcurrentHashMap<String, UnifiedAssistant> assistants = new ConcurrentHashMap<>();

    public UnifiedChatService(
            OpenAiChatModel chatModel,
             ContentRetriever contentRetriever,
             WebSearchTool webSearchTool) {
        this.chatModel = chatModel;
        this.contentRetriever = contentRetriever;
        this.webSearchTool = webSearchTool;

        log.info("UnifiedChatService initialized with capabilities: RAG={}, WebSearch={}",
                contentRetriever != null, webSearchTool != null);
    }

    public String chat(String sessionId, String userMessage) {
        UnifiedAssistant assistant = assistants.computeIfAbsent(sessionId, this::createAssistant);

        // Inject current date so AI knows what "today" means
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        String response = assistant.chat(currentDate, userMessage);
        log.debug("Session {}: user='{}', assistant='{}'", sessionId, userMessage, response);
        return response;
    }

    public void clearMemory(String sessionId) {
        assistants.remove(sessionId);
        log.info("Cleared memory for unified chat session {}", sessionId);
    }

    private UnifiedAssistant createAssistant(String sessionId) {
        ChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(40)
                .build();

        return AiServices.builder(UnifiedAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(memory)
                .contentRetriever(contentRetriever)
                .tools(webSearchTool)
                .build();
    }

    interface UnifiedAssistant {
        @SystemMessage({
                "Current date: {{current_date}}",
                "",
                "You are Ray's intelligent AI assistant with multiple powerful capabilities:",
                "",
                "## Your Capabilities:",
                "1. **Document Retrieval (RAG)**: You can search and retrieve information from documents the user has uploaded",
                "2. **Web Search**: You can search the internet for current, real-time information",
                "3. **Base Knowledge**: Your extensive training data for general questions and explanations",
                "",
                "## When to Use Each Capability:",
                "",
                "**Use Document Retrieval when:**",
                "- User asks about uploaded files, documents, or PDFs",
                "- Questions like 'What does my report say...', 'According to my document...'",
                "- User references specific files they've uploaded",
                "- You need to find specific data or quotes from their documents",
                "",
                "**Use Web Search when:**",
                "- User asks about current events, recent news, or 'latest' information",
                "- Questions about 'today', 'now', 'this week', or other time-sensitive queries",
                "- Weather forecasts, stock prices, sports scores, or real-time data",
                "- Information that changes frequently or is more recent than your training data",
                "",
                "**Use Base Knowledge when:**",
                "- General knowledge questions (history, science, concepts)",
                "- Explanations, how-to guides, or educational content",
                "- Creative tasks like writing, brainstorming, or problem-solving",
                "- The question doesn't require current data or specific uploaded documents",
                "",
                "## Important Rules:",
                "- You can use MULTIPLE capabilities in a single response if needed",
                "- Always cite your sources when using document retrieval or web search",
                "- If you use documents, mention which document the information came from",
                "- If you use web search, include the source URLs or website names",
                "- Be transparent about which capability you're using and why",
                "",
                "Your name is Ray's Unified Agent. Be helpful, accurate, and always cite your sources!"
        })
        String chat(@V("current_date") String currentDate,
                    @UserMessage String userMessage);
    }
}
