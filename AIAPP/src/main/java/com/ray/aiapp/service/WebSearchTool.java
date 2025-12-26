package com.ray.aiapp.service;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Web search tool that can be used by AI agents to search the internet.
 * The AI will automatically decide when to use this tool based on the user's query.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "aiapp.langchain.web-search", name = "enabled", havingValue = "true")
public class WebSearchTool {

    private final WebSearchEngine searchEngine;
    private final int maxResults;

    public WebSearchTool(WebSearchEngine searchEngine,
                        com.ray.aiapp.config.properties.LangchainModelProperties properties) {
        this.searchEngine = searchEngine;
        this.maxResults = properties.getWebSearch().getMaxResults();
    }

    /**
     * Searches the web for current, real-time information.
     * The AI agent will automatically call this when it needs up-to-date information
     * that's not in its training data.
     *
     * @param query The search query (e.g., "current weather in Tokyo", "latest news about AI")
     * @return Formatted search results with titles, snippets, and URLs
     */
    @Tool("Searches the internet for current information about any topic. " +
          "Use this when you need real-time data, recent news, current events, " +
          "weather, stock prices, or any information that changes over time.")
    public String searchWeb(String query) {
        log.info("Web search requested: {}", query);

        WebSearchRequest webSearchRequest = WebSearchRequest.builder()
                .searchTerms(query)
                .maxResults(maxResults)
                .build();

        WebSearchResults webSearchResults = searchEngine.search(webSearchRequest);

        if (webSearchResults.results().isEmpty()) {
            log.warn("No search results found for query: {}", query);
            return "No results found for: " + query;
        }

        log.info("Found {} search results for query: {}", webSearchResults.results().size(), query);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < webSearchResults.results().size(); i++) {
            WebSearchOrganicResult result = webSearchResults.results().get(i);
            sb.append(i + 1).append(". [")
                    .append(result.title()).append("](")
                    .append(result.url()).append(")\n   ")
                    .append(result.snippet())
                    .append("\n\n");
        }
        return sb.toString();
    }
}
