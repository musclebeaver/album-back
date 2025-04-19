package site.musclebeaver.album.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.user.dto.LoginRequestDto;
import site.musclebeaver.album.user.dto.JwtResponse; // ✅ 추가
import site.musclebeaver.album.user.entity.UserEntity;
import site.musclebeaver.album.user.service.UserService;
import site.musclebeaver.album.security.util.JwtTokenProvider;

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

    // ✅ 로그인 처리 API (JWT + userId 반환)
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

            String jwt = jwtTokenProvider.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(
                    new JwtResponse(jwt, userEntity.getId(), userEntity.isApproved(), userEntity.isAdmin())
            );

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
