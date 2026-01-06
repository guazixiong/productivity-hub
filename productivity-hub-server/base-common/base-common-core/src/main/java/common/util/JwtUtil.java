package common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
public class JwtUtil {

    /**
     * 默认密钥（生产环境应使用配置）
     */
    private static final String SECRET = "productivity-hub-secret-key-2025";

    /**
     * 默认过期时间（7天，单位：毫秒）
     */
    private static final long DEFAULT_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param expireDays 过期天数（如果为null，使用默认7天）
     * @return Token 字符串
     */
    public static String generateToken(String userId, String username, Integer expireDays) {
        long expireTime = expireDays != null ? expireDays * 24 * 60 * 60 * 1000L : DEFAULT_EXPIRE_TIME;
        Date expireDate = new Date(System.currentTimeMillis() + expireTime);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    /**
     * 生成 Token（使用默认过期时间）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Token 字符串
     */
    public static String generateToken(String userId, String username) {
        return generateToken(userId, username, null);
    }

    /**
     * 从 Token 中获取 Claims
     *
     * @param token Token 字符串
     * @return Claims 对象
     */
    public static Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析 Token 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token Token 字符串
     * @return 用户ID
     */
    public static String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("userId") : null;
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token Token 字符串
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token Token 字符串
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            log.error("验证 Token 失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token Token 字符串
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return true;
            }
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从请求头中提取 Token
     *
     * @param authHeader Authorization 请求头（格式：Bearer {token}）
     * @return Token 字符串，如果格式不正确返回 null
     */
    public static String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

