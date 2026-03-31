package com.indona.invento.services.impl;

import io.jsonwebtoken.Claims; 
import io.jsonwebtoken.Jwts; 
import io.jsonwebtoken.SignatureAlgorithm; 
import io.jsonwebtoken.io.Decoders; 
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.indona.invento.services.JwtService;

import java.security.Key; 
import java.util.Date; 
import java.util.HashMap; 
import java.util.Map; 
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService { 

	@Autowired
	private UserInfoServiceImpl userInfoService;
	
	public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

	@Override
	public String generateToken(String userName) {
		Map<String, Object> claims = new HashMap<>();

		Map<String, Object> userInfo = userInfoService.findUserByUserName(userName);
		claims.put("userDetails", userInfo);
		return createToken(claims, userName);
	}


	private String createToken(Map<String, Object> claims, String userName) { 
		return Jwts.builder() 
				.setClaims(claims) 
				.setSubject(userName) 
				.setIssuedAt(new Date(System.currentTimeMillis())) 
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) 
				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact(); 
	} 

	private Key getSignKey() { 
		byte[] keyBytes= Decoders.BASE64.decode(SECRET); 
		return Keys.hmacShaKeyFor(keyBytes); 
	} 

	@Override
	public String extractUsername(String token) { 
		return extractClaim(token, Claims::getSubject); 
	} 

	@Override
	public Date extractExpiration(String token) { 
		return extractClaim(token, Claims::getExpiration); 
	} 

	@Override
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) { 
		final Claims claims = extractAllClaims(token); 
		return claimsResolver.apply(claims); 
	} 

	private Claims extractAllClaims(String token) { 
		return Jwts 
				.parserBuilder() 
				.setSigningKey(getSignKey()) 
				.build() 
				.parseClaimsJws(token) 
				.getBody(); 
	} 
	
	private Boolean isTokenExpired(String token) { 
		return extractExpiration(token).before(new Date()); 
	} 

	@Override
	public Boolean validateToken(String token, UserDetails userDetails) { 
		final String username = extractUsername(token); 
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)); 
	} 


} 
