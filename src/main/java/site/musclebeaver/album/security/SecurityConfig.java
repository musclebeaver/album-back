package site.musclebeaver.album.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import site.musclebeaver.album.security.filter.JwtAuthenticationFilter;
import site.musclebeaver.album.security.filter.JwtAuthorizationFilter;
import site.musclebeaver.album.security.util.JwtTokenProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // PasswordEncoder Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 비밀번호 암호화 방식
    }

    // AuthenticationManager 설정
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManager.class);
    }

    // HTTP 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()  // CSRF 보호 비활성화 (API 사용 시)
                .authorizeHttpRequests()
                .requestMatchers("/login", "/register").permitAll()  // 로그인, 회원가입은 누구나 접근 허용
                .anyRequest().authenticated()  // 나머지 요청은 인증 필요
                .and()
//                .addFilter(new JwtAuthenticationFilter(authenticationManager(http)))  // 로그인 요청 처리 필터
                .addFilter(new JwtAuthorizationFilter("/api/**", authenticationManager(http), jwtTokenProvider, userDetailsService));  // JwtAuthorizationFilter를 추가  // JWT 인증 필터
        return http.build();
    }
}
