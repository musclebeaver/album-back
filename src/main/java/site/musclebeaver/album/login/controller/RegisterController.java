package site.musclebeaver.album.login.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.login.dto.SignUpRequestDto;
import site.musclebeaver.album.login.entity.UserEntity;
import site.musclebeaver.album.login.service.UserService;

@RestController
@RequestMapping("/api")
public class RegisterController {

    @Autowired
    private UserService userService;

    // 회원가입 처리 API
    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody SignUpRequestDto signUpRequest) {
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
}
