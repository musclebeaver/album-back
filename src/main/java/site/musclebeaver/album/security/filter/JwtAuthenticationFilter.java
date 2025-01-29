package site.musclebeaver.album.security.filter;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import site.musclebeaver.album.security.util.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthorizationFilter(String defaultFilterProcessesUrl,
                                  AuthenticationManager authenticationManager,
                                  JwtTokenProvider jwtTokenProvider,
                                  UserDetailsService userDetailsService) {
        super(defaultFilterProcessesUrl); // "/api/**" 또는 다른 URL 패턴을 지정
        setAuthenticationManager(authenticationManager);  // AuthenticationManager 설정
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);  // "Bearer " 이후의 토큰 부분만 가져오기
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);  // JWT에서 사용자 정보 가져오기
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);  // 사용자 정보 로드

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                return authentication;  // 인증을 진행하고 반환
            }
        }

        return null;  // 인증 실패 시
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);  // 인증된 사용자 정보 저장
        chain.doFilter(request, response);  // 필터 체인 계속 진행
    }
}
