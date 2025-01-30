package site.musclebeaver.album.login.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.login.dto.LoginRequestDto;
import site.musclebeaver.album.login.entity.UserEntity;
import site.musclebeaver.album.login.service.UserService;
import site.musclebeaver.album.security.CustomUserDetailsService;
import site.musclebeaver.album.security.util.JwtTokenProvider;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    // 로그인 처리 API
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequest) {
        // 로그인 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 인증 성공 시 사용자 정보 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        authentication.getPrincipal();

        // 관리자 승인 여부 확인
        UserEntity userEntity = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!userEntity.isApproved()) {
            return ResponseEntity.status(403).body("User not approved by admin");
        }

        // JWT 생성
        String jwt = jwtTokenProvider.generateToken(userDetails.getUsername());

        // JWT 토큰을 응답으로 반환
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

        // JWT 응답을 담을 DTO
    public static class JwtResponse {
        private String token;

        public JwtResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}
