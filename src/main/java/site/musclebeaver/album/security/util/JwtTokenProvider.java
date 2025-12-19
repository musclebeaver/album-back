package site.musclebeaver.album.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    // ✅ 수정 후 (30분)
    private final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 30;
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("JWT Secret Key is missing or empty! Check application.properties");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // ✅ Access Token 생성
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Refresh Token 생성
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ 토큰에서 사용자 이름 추출
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("❌ JWT 만료됨: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("❌ 지원되지 않는 JWT: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("❌ 잘못된 형식의 JWT: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("❌ 서명 검증 실패: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ 잘못된 인자: " + e.getMessage());
        }
        return false;
    }

    // ✅ HTTP 요청에서 JWT 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
