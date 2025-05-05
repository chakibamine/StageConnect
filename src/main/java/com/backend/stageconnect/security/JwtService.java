package com.backend.stageconnect.security;

import com.backend.stageconnect.entity.User;
import com.backend.stageconnect.entity.UserType;
import com.backend.stageconnect.entity.Responsible;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret:defaultSecretKey123456789012345678901234567890}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long jwtExpiration;
    
    private Key key;
    
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        
        // Add company_id to claims if user is a RESPONSIBLE
        if (user.getUserType() == UserType.RESPONSIBLE) {
            Responsible responsible = (Responsible) user;
            if (responsible.getCompany() != null) {
                extraClaims.put("company_id", responsible.getCompany().getId());
            }
        }
        
        return generateToken(extraClaims, user);
    }
    
    public String generateToken(Map<String, Object> extraClaims, User user) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("userType", user.getUserType().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }
    
    public String extractUserType(String token) {
        return extractAllClaims(token).get("userType", String.class);
    }
    
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getEmail())) && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
} 