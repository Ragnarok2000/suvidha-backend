package com.suvidha.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Service
public class PerplexityService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final WebClient webClient;

    public PerplexityService(@Value("${perplexity.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.perplexity.ai")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Cacheable(value = "generatedSummaries", key = "#inputText + '-' + #summaryLength", unless = "#result == null || #result.contains('Error')")
    public String summarize(String inputText, String summaryLength) {
        System.out.println("⏳ Redis NOT used — calling Perplexity API for: " + inputText + " with length: " + summaryLength);

        // ✅ Strict word count instructions
        String lengthInstruction;
        int maxWords;
        
        switch (summaryLength != null ? summaryLength.toLowerCase() : "medium") {
            case "short":
                lengthInstruction = "Provide a concise summary in EXACTLY 50-150 words. Do not exceed 150 words";
                maxWords = 150;
                break;
            case "long":
                lengthInstruction = "Provide a detailed summary in EXACTLY 250-500 words. Do not exceed 500 words";
                maxWords = 500;
                break;
            case "medium":
            default:
                lengthInstruction = "Provide a summary in EXACTLY 150-250 words. Do not exceed 250 words";
                maxWords = 250;
                break;
        }

        Map<String, Object> requestBody = Map.of(
            "model", "sonar",
            "messages", List.of(
                Map.of("role", "system", "content", "You are a helpful assistant that summarizes text in a clear, engaging, and friendly tone. Always respect word count limits strictly."),
                Map.of("role", "user", "content", lengthInstruction + ". Please summarize the following text:\n\n" + inputText)
            )
        );

        JsonNode response = webClient.post()
            .uri("/chat/completions")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .doOnNext(json -> System.out.println("Perplexity response: " + json.toPrettyString()))
            .block();

        try {
            String summary = response.get("choices").get(0).get("message").get("content").asText();
            
            // ✅ NEW: Enforce word limit by truncating if needed
            summary = enforceWordLimit(summary, maxWords);
            
            return summary;
        } catch (Exception e) {
            System.err.println("Failed to parse Perplexity response: " + e.getMessage());
            return "Error: Unable to parse summary from Perplexity response.";
        }
    }

    // ✅ NEW: Truncate summary to max word count
    private String enforceWordLimit(String text, int maxWords) {
        if (text == null || text.isEmpty()) return text;
        
        String[] words = text.trim().split("\\s+");
        
        if (words.length <= maxWords) {
            return text; // Already within limit
        }
        
        // Truncate to max words and add ellipsis
        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            truncated.append(words[i]).append(" ");
        }
        
        // Remove trailing space and add period if missing
        String result = truncated.toString().trim();
        if (!result.endsWith(".") && !result.endsWith("!") && !result.endsWith("?")) {
            result += "...";
        }
        
        System.out.println("⚠️ Summary truncated from " + words.length + " to " + maxWords + " words");
        return result;
    }

    // Overloaded method for backward compatibility
    public String summarize(String inputText) {
        return summarize(inputText, "medium");
    }

    public String fetchContentFromUrl(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.body().text();
        } catch (Exception e) {
            System.err.println("Failed to fetch content from URL: " + e.getMessage());
            return "Error: Unable to fetch content from URL.";
        }
    }
}
