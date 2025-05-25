package site.musclebeaver.album.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.security.util.JwtTokenProvider;
import site.musclebeaver.album.user.entity.UserEntity;
import site.musclebeaver.album.user.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthController(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    // ✅ Refresh 토큰으로 Access 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid Refresh Token");
            return ResponseEntity.status(401).body(errorResponse);
        }

        return userService.findByRefreshToken(refreshToken)
                .map(user -> {
                    String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername());

                    Map<String, String> response = new HashMap<>();
                    response.put("accessToken", newAccessToken);

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid Refresh Token");
                    return ResponseEntity.status(401).body(errorResponse);
                });
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        return userService.findByRefreshToken(refreshToken)
                .map(user -> {
                    user.setRefreshToken(null); // ✅ refreshToken 무효화
                    userService.save(user);
                    return ResponseEntity.ok("로그아웃 성공");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자를 찾을 수 없습니다."));
    }
}
