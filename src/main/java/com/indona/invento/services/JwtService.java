package com.indona.invento.services;

import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;

public interface JwtService {

	Boolean validateToken(String token, UserDetails userDetails);

	String generateToken(String userName);
	
	String extractUsername(String token);
	
	Date extractExpiration(String token);
	
	<T> T extractClaim(String token, Function<Claims, T> claimsResolver);

}
