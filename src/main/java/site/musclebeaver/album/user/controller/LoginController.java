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

    // ✅ 로그인 처리 API (Access + Refresh 발급)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserEntity userEntity = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!userEntity.isApproved()) {
                return ResponseEntity.status(403).body("NOT_APPROVED");
            }

            // 로그인 성공 → 실패 카운트 초기화
            userService.resetFailedLoginCount(userEntity);

            // ✅ AccessToken + RefreshToken 발급
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails.getUsername());
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUsername());

            // ✅ RefreshToken DB에 저장
            userService.updateRefreshToken(userDetails.getUsername(), refreshToken);

            // ✅ 응답 JSON 구성
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("userId", userEntity.getId());
            response.put("approved", userEntity.isApproved());
            response.put("isAdmin", userEntity.isAdmin());

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            // 실패한 사용자 이름이 DB에 있으면 카운트 증가
            userService.findByUsername(loginRequest.getUsername()).ifPresent(user -> {
                userService.increaseFailedLoginCount(user);
            });

            Optional<UserEntity> userOpt = userService.findByUsername(loginRequest.getUsername());
            if (userOpt.isPresent() && userOpt.get().getFailedLoginCount() >= 5) {
                return ResponseEntity.status(403).body("TOO_MANY_FAILED_ATTEMPTS");
            }

            return ResponseEntity.status(401).body("INVALID_CREDENTIALS");
        }
    }
}
