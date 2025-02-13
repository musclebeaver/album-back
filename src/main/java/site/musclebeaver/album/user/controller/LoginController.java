package site.musclebeaver.album.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.user.dto.LoginRequestDto;
import site.musclebeaver.album.user.entity.UserEntity;
import site.musclebeaver.album.user.service.UserService;
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

    // ✅ 로그인 처리 API (JWT 발급)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequest) {
        // 1️⃣ 로그인 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2️⃣ 인증된 사용자 정보 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3️⃣ DB에서 사용자 정보 조회 및 관리자 승인 여부 확인
        UserEntity userEntity = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!userEntity.isApproved()) {
            return ResponseEntity.status(403).body("User not approved by admin");
        }

        // 4️⃣ JWT 생성 후 응답 반환
        String jwt = jwtTokenProvider.generateToken(userDetails.getUsername());
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    // ✅ JWT 응답 DTO (토큰 반환)
    public static class JwtResponse {
        private final String token;

        public JwtResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}
