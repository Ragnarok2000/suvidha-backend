package com.suvidha.Service;

import java.util.List;
import java.util.Optional;

import com.suvidha.Modal.Article;
import com.suvidha.Modal.User;

public interface IArticleService {
	
	Article save(Article article);
	
	 List<Article> getAll();
	 
	 Optional<Article> getById(Long id);
	 
	 boolean deleteById(Long id, String username);
	 
	  List<Article> getByUser(User user);
		    
		

}
