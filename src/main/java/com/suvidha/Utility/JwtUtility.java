package com.suvidha.Utility;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.suvidha.Modal.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtility {
	
	@Value("${jwt.secret}")
    private String secret;

    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

	
	public boolean validateToken(String token, User user) {
	    final String username = extractUsername(token);
	    return (username.equals(user.getUsername()) && !isTokenExpired(token));
	}

	private boolean isTokenExpired(String token) {
	    return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
	    return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getExpiration();
	}


}
