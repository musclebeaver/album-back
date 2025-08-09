package site.musclebeaver.album.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.api.dto.ChangePasswordRequestDto;
import site.musclebeaver.album.api.dto.UserResponseDto;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.security.CustomUserDetails;
import site.musclebeaver.album.security.util.JwtTokenProvider;
import site.musclebeaver.album.api.service.UserService;

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

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserEntity user = userService.getUserById(userDetails.getId());
        UserResponseDto dto = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isApproved(),
                user.isAdmin(),
                user.getFailedLoginCount()
        );

        return ResponseEntity.ok(dto);
    }


    // ✅ 비밀번호 변경
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChangePasswordRequestDto request
    ) {
        try {
            userService.changePassword(userDetails.getId(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("비밀번호가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            // 현재 비밀번호 불일치, 유효성 실패 등
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경 중 오류가 발생했습니다.");
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        // refreshToken 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        // refresh token 무효화
        userService.invalidateRefreshToken(refreshToken);

        return ResponseEntity.ok("로그아웃 성공");
    }

}
