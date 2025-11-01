package com.suvidha.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.suvidha.Modal.User;
import com.suvidha.Repsoitory.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IuserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public User save(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	@Override
	public User findByUsername(String username) {
  
		return userRepository.findByUsername(username);
	}

}
