package com.suvidha.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.suvidha.Modal.Article;
import com.suvidha.Modal.User;
import com.suvidha.Service.ArticleService;
import com.suvidha.Service.PerplexityService;
import com.suvidha.Service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final UserService userService;
    private final PerplexityService perplexityService;
    
    @PostMapping("/summarize/{id}")
    public ResponseEntity<Article> summarizeAndSave(
            @PathVariable Long id,
            @RequestParam(defaultValue = "medium") String summaryLength) {
        Optional<Article> articleOpt = articleService.getById(id);
        if (articleOpt.isPresent()) {
            Article article = articleOpt.get();
            String summary = perplexityService.summarize(article.getContent(), summaryLength);
            article.setSummary(summary);
            article.setSummaryLength(summaryLength);
            // createdAt auto-set by @PrePersist on first save
            Article updated = articleService.save(article);
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<Article>> getUserArticles(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<Article> articles = articleService.getByUser(user);
        return ResponseEntity.ok(articles);
    }

    @PostMapping("/create")
    public ResponseEntity<Article> createArticle(@RequestBody Article article, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        article.setUser(user);
        Article saved = articleService.save(article);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Article>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAll());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        return articleService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/summarizeFromLink")
    public ResponseEntity<Article> summarizeFromLink(@RequestBody Map<String, String> request, Principal principal) {
        User user = userService.findByUsername(principal.getName());

        String url = request.get("url");
        String summaryLength = request.getOrDefault("summaryLength", "medium");

        // Step 1: Fetch content from the URL
        String content = perplexityService.fetchContentFromUrl(url);

        // Step 2: Summarize the content
        String summary = perplexityService.summarize(content, summaryLength);

        // Step 3: Save the article
        Article article = new Article();
        article.setUrl(url);
        article.setContent(content);
        article.setSummary(summary);
        article.setSummaryLength(summaryLength);
        article.setUser(user);
        // createdAt auto-set by @PrePersist

        Article saved = articleService.save(article);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<String> deleteArticle(@PathVariable Long id, Principal principal) {
        boolean deleted = articleService.deleteById(id, principal.getName());
        if (deleted) {
            return ResponseEntity.ok("Article deleted successfully.");
        } else {
            return ResponseEntity.status(404).body("Article not found or unauthorized.");
        }
    }
}
