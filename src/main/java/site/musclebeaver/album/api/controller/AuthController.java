package site.musclebeaver.album.api.controller;

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
}
