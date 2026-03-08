package com.cdc.usercenter.service.impl;

import com.cdc.usercenter.common.BusinessException;
import com.cdc.usercenter.common.ResultCode;
import com.cdc.usercenter.entity.SysUser;
import com.cdc.usercenter.service.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token服务实现
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;
    
    @Value("${jwt.issuer}")
    private String issuer;
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String TOKEN_PREFIX = "user:token:";
    
    public TokenServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public String generateAccessToken(SysUser user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSignKey())
                .compact();
    }
    
    @Override
    public String generateRefreshToken(SysUser user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSignKey())
                .compact();
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Token无效: {}", e.getMessage());
        }
        return false;
    }
    
    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }
    
    @Override
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }
    
    @Override
    public String refreshToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        Long userId = getUserIdFromToken(refreshToken);
        String storedToken = getTokenFromRedis(userId);
        if (!refreshToken.equals(storedToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        // 返回新的access token
        return generateAccessToken(null); // 简化实现
    }
    
    @Override
    public void saveTokenToRedis(Long userId, String token) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public String getTokenFromRedis(Long userId) {
        String key = TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }
    
    @Override
    public void deleteTokenFromRedis(Long userId) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
