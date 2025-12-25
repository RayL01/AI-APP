package com.ray.aiapp.service;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

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

        // TODO(human): Implement the search logic here
        // Step 1: Call searchEngine.search(query, maxResults) to get WebSearchResults
        // Step 2: Extract organic results from WebSearchResults using .results()
        // Step 3: Format the results into a readable string with titles, snippets, and URLs
        // Step 4: Handle the case when no results are found
        // Step 5: Return the formatted string
        //
        // Hint: WebSearchResults has a method results() that returns List<WebSearchOrganicResult>
        // Each result has: title(), snippet(), url()
        // Format like: "1. [Title](URL)\n   Snippet\n\n2. [Title](URL)\n   Snippet..."

        throw new UnsupportedOperationException("TODO: Implement web search logic");
    }
}
