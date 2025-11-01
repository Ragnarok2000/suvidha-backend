package com.suvidha.Service;

import java.util.List;

import com.suvidha.Exception.SummaryNotFoundException;
import com.suvidha.Modal.Summary;


public interface IsummaryService {
	
	List<Summary> getAll();
	
	Summary save(Summary summary);
	
	boolean deleteById(Long id,String username);
	
	List<Summary> getSummariesByUsername(String username) throws SummaryNotFoundException;
	
	
	

}
