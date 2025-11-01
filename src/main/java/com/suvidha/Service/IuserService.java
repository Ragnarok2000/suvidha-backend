package com.suvidha.Service;

import com.suvidha.Modal.User;

public interface IuserService {
	
	User save(User user);
	
	User findByUsername(String username);

}
