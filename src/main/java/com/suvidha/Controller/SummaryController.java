package com.suvidha.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suvidha.Exception.SummaryNotFoundException;
import com.suvidha.Modal.Summary;
import com.suvidha.Modal.User;
import com.suvidha.Service.PerplexityService;
import com.suvidha.Service.SummaryService;
import com.suvidha.Service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;
    private final PerplexityService perplexityService;
    private final UserService userService;
    private final CacheManager cacheManager; // ✅ Added for cache management

    @GetMapping("/getAllSummaries")
    public List<Summary> getAllSummary() {
        return summaryService.getAll(); // Returns all summaries (admin/internal use)
    }

    @GetMapping("/history")
    public ResponseEntity<List<Summary>> getUserSummaries(Principal principal) throws SummaryNotFoundException {
        List<Summary> summaries = summaryService.getSummariesByUsername(principal.getName());
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/createSummary")
    public ResponseEntity<?> createSummary(@RequestBody Map<String, String> request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);

        String originalText = request.get("originalText");
        String summaryLength = request.getOrDefault("summaryLength", "medium"); // Default to "medium"

        String generatedSummary = perplexityService.summarize(originalText, summaryLength);

        // Creating a new Summary object to ensure unique DB entry
        Summary newSummary = new Summary();
        newSummary.setOriginalText(originalText);
        newSummary.setSummaryText(generatedSummary);
        newSummary.setSummaryLength(summaryLength);
        newSummary.setUser(user);
        // createdAt is auto-set by @PrePersist

        summaryService.save(newSummary);

        return ResponseEntity.ok(Map.of(
            "summaryText", generatedSummary,
            "summaryLength", summaryLength,
            "createdAt", newSummary.getCreatedAt()
        ));
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id, Principal principal) {
        boolean deleted = summaryService.deleteById(id, principal.getName());
        if (deleted) {
            return ResponseEntity.ok("Summary deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Summary not found or unauthorized.");
        }
    }

    // ✅ NEW ENDPOINT: Clear Redis cache to fix old cached data
    @GetMapping("/clearCache")
    public ResponseEntity<String> clearCache() {
        try {
            cacheManager.getCache("generatedSummaries").clear();
            return ResponseEntity.ok("✅ Cache cleared successfully! You can now generate summaries with different lengths.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("❌ Failed to clear cache: " + e.getMessage());
        }
    }

}
