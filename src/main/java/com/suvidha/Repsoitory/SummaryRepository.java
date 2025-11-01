package com.suvidha.Repsoitory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suvidha.Modal.Summary;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {
	
	List<Summary> findByUserUsername(String username);
	
	


}
