package site.musclebeaver.album.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.api.dto.LoginRequestDto;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.api.service.UserService;
import site.musclebeaver.album.security.util.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder; // ✅ 주입



    // ✅ 로그인 처리 API (Access + Refresh 발급)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequest) {
        try {

            // ✅ 공백 방지
            final String username = loginRequest.getUsername() == null ? "" : loginRequest.getUsername().trim();
            final String rawPw    = loginRequest.getPassword() == null ? "" : loginRequest.getPassword().trim();

            // ✅ 임시: 수동 매칭 검사 (디버깅용)
            userService.findByUsername(username).ifPresent(u -> {
                boolean m = passwordEncoder.matches(rawPw, u.getPassword());
                System.out.println("[LOGIN DEBUG] username=" + u.getUsername() + ", manualMatches=" + m);
            });

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, rawPw)
            );


            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserEntity userEntity = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!userEntity.isApproved()) {
                // ✅ 200 OK + success: false로
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "NOT_APPROVED");
                return ResponseEntity.ok(errorResponse);
            }

            userService.resetFailedLoginCount(userEntity);
            // (이제 토큰 안에 ROLE_ADMIN 같은 권한 정보가 들어갑니다)
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUsername());
            userService.updateRefreshToken(userDetails.getUsername(), refreshToken);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("accessToken", accessToken);
            successResponse.put("refreshToken", refreshToken);
            successResponse.put("userId", userEntity.getId());
            successResponse.put("approved", userEntity.isApproved());
            successResponse.put("isAdmin", userEntity.isAdmin());

            return ResponseEntity.ok(successResponse);

        } catch (Exception ex) {
            // 로그인 실패 처리 (비밀번호 틀림 등)
            userService.findByUsername(loginRequest.getUsername()).ifPresent(userService::increaseFailedLoginCount);

            Optional<UserEntity> userOpt = userService.findByUsername(loginRequest.getUsername());
            if (userOpt.isPresent() && userOpt.get().getFailedLoginCount() >= 5) {
                Map<String, Object> tooManyAttemptsResponse = new HashMap<>();
                tooManyAttemptsResponse.put("success", false);
                tooManyAttemptsResponse.put("error", "TOO_MANY_FAILED_ATTEMPTS");
                return ResponseEntity.ok(tooManyAttemptsResponse);
            }

            Map<String, Object> invalidCredentialsResponse = new HashMap<>();
            invalidCredentialsResponse.put("success", false);
            invalidCredentialsResponse.put("error", "INVALID_CREDENTIALS");
            return ResponseEntity.ok(invalidCredentialsResponse);
        }
    }

}
