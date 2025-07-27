package site.musclebeaver.album.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.api.dto.SignUpRequestDto;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.api.service.UserService;

@RestController
@RequestMapping("/api")
public class RegisterController {

    @Autowired
    private UserService userService;

    // 회원가입 처리 API
    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody SignUpRequestDto signUpRequest) {
        System.out.println("👉 수신된 요청: " + signUpRequest);
        UserEntity newUser = userService.registerUser(signUpRequest);
        return ResponseEntity.ok(newUser);
    }

        // ✅ username 중복 확인 API
    @GetMapping("/checkusername")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean isTaken = userService.existsByUsername(username);
        if (isTaken) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        return ResponseEntity.ok("Username is available");
    }

    // ✅ email 중복 확인 API 추가
    @GetMapping("/checkemail")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean isTaken = userService.existsByEmail(email);
        if (isTaken) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }
        return ResponseEntity.ok("Email is available");
    }
}
