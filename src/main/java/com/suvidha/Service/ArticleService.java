package com.suvidha.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.suvidha.Modal.Article;
import com.suvidha.Modal.User;
import com.suvidha.Repsoitory.ArticleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService implements IArticleService {

    private final ArticleRepository articleRepository;
    private final PerplexityService perplexityService; // Injecting PerplexityService

    @Override
    public Article save(Article article) {
        // ✅ Generate summary using Perplexity AI
        String summary = perplexityService.summarize(article.getContent());
        article.setSummary(summary);

        return articleRepository.save(article);
    }

    @Override
    public List<Article> getAll() {
        return articleRepository.findAll();
    }

    @Override
    public Optional<Article> getById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    public boolean deleteById(Long id, String username) {
        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isPresent()) {
            Article article = articleOpt.get();
            if (article.getUser().getUsername().equals(username)) {
                articleRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

	@Override
	public List<Article> getByUser(User user) {
		 return articleRepository.findByUser(user);

	}
}