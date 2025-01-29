package site.musclebeaver.album.security.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "your_secret_key";  // JWT 서명에 사용할 비밀 키
    private final long EXPIRATION_TIME = 86400000L;  // 만료 시간 (1일)

    // JWT 토큰 생성
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)  // 사용자 이름을 토큰의 subject로 설정
                .setIssuedAt(new Date())  // 토큰 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)  // 비밀 키로 서명
                .compact();
    }

    // JWT 토큰에서 사용자 이름 가져오기
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWT 토큰이 유효한지 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);  // 토큰을 파싱하여 검증
            return true;
        } catch (Exception e) {
            return false;  // 검증 실패 시 false 반환
        }
    }
}
