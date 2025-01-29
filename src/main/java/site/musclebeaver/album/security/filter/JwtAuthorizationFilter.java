package site.musclebeaver.album.security.filter;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import site.musclebeaver.album.security.util.JwtTokenProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthorizationFilter(String defaultFilterProcessesUrl, UserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider) {
        super(defaultFilterProcessesUrl);  // 기본적으로 필터가 작동할 URL 경로 지정
        this.jwtTokenProvider = jwtTokenProvider;  // JWT 토큰 검증을 위한 유틸리티 클래스
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String header = request.getHeader("Authorization");

        // 헤더에 JWT가 없거나 "Bearer "로 시작하지 않으면 인증을 건너뛰어 다음 필터로 넘김
        if (header == null || !header.startsWith("Bearer ")) {
            return null; // 인증을 하지 않음
        }

        // JWT 토큰을 파싱하고 유효한지 검증
        String token = header.substring(7);  // "Bearer " 이후의 토큰 부분만 가져오기
        if (jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);  // JWT에서 사용자 정보 가져오기
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);  // 사용자 정보 로드

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext에 인증 정보를 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return authentication; // 인증이 완료되면 인증 객체 반환
        }

        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult); // 인증 성공 시 후속 처리를 수행
        chain.doFilter(request, response);  // 필터 체인 계속 진행
    }
}
