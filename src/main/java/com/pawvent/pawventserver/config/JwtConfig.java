// package com.pawvent.pawventserver.config;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;

// import java.security.Key;
// import java.util.Date;
// import java.util.function.Function;

// @Slf4j
// @Component
// public class JwtConfig {

//     @Value("${jwt.secret}")
//     private String secretKey;

//     @Value("${jwt.expiration}")
//     private Long expiration;

//     private Key getSigningKey() {
//         return Keys.hmacShaKeyFor(secretKey.getBytes());
//     }

//     public String generateToken(String userId) {
//         return Jwts.builder()
//                 .setSubject(userId)
//                 .setIssuedAt(new Date(System.currentTimeMillis()))
//                 .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                 .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                 .compact();
//     }

//     public String extractUserId(String token) {
//         return extractClaim(token, Claims::getSubject);
//     }

//     public Date extractExpiration(String token) {
//         return extractClaim(token, Claims::getExpiration);
//     }

//     public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//         final Claims claims = extractAllClaims(token);
//         return claimsResolver.apply(claims);
//     }

//     private Claims extractAllClaims(String token) {
//         return Jwts.parserBuilder()
//                 .setSigningKey(getSigningKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     public Boolean isTokenExpired(String token) {
//         return extractExpiration(token).before(new Date());
//     }

//     public Boolean validateToken(String token, String userId) {
//         final String extractedUserId = extractUserId(token);
//         return (extractedUserId.equals(userId) && !isTokenExpired(token));
//     }
// }
