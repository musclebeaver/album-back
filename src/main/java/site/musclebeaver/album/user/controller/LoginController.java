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
                // ✅ 200 OK + success: false로
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "NOT_APPROVED");
                return ResponseEntity.ok(errorResponse);
            }

            userService.resetFailedLoginCount(userEntity);

            String accessToken = jwtTokenProvider.generateAccessToken(userDetails.getUsername());
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
