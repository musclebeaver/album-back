package site.musclebeaver.album.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.api.dto.UserUpdateRequestDto;
import site.musclebeaver.album.api.service.AdminUserService;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.api.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserService userService;

    /** ✅ 검색 타입과 키워드를 쿼리스트링으로 받음 */
    @GetMapping
    public ResponseEntity<?> getUsers(@PageableDefault(size = 20) Pageable pageable,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String searchType,
                                      Authentication authentication) {
        verifyAdmin(authentication);
        return ResponseEntity.ok(adminUserService.getPagedUsers(pageable, searchType, keyword));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId, Authentication authentication) {
        verifyAdmin(authentication);
        return ResponseEntity.ok(adminUserService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId,
                                        @RequestBody UserUpdateRequestDto request,
                                        Authentication authentication) {
        verifyAdmin(authentication);
        adminUserService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, Authentication authentication) {
        verifyAdmin(authentication);
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long userId, Authentication authentication) {
        verifyAdmin(authentication);
        adminUserService.resetPassword(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/reset-fail-count")
    public ResponseEntity<?> resetFailedLoginCount(@PathVariable Long userId, Authentication authentication) {
        verifyAdmin(authentication);
        adminUserService.resetFailedLoginCount(userId);
        return ResponseEntity.ok().build();
    }

    private void verifyAdmin(Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        if (!user.isAdmin()) {
            throw new SecurityException("관리자 권한이 필요합니다.");
        }
    }
}
