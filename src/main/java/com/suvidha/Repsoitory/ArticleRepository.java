package com.suvidha.Repsoitory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suvidha.Modal.Article;
import com.suvidha.Modal.User;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
	
	List<Article> findByUserUsername(String username);
	
	List<Article> findByUser(User user);


}
