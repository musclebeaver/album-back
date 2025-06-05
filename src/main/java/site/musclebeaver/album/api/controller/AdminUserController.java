package site.musclebeaver.album.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import site.musclebeaver.album.api.dto.UserUpdateRequestDto;
import site.musclebeaver.album.api.service.AdminUserService;
import site.musclebeaver.album.user.entity.UserEntity;
import site.musclebeaver.album.user.service.UserService;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserService userService;

    /** ✅ 서버 사이드 페이징 회원 조회 */
    @GetMapping
    public ResponseEntity<?> getUsers(@PageableDefault(size = 20) Pageable pageable,
                                      Authentication authentication) {
        verifyAdmin(authentication);
        return ResponseEntity.ok(adminUserService.getPagedUsers(pageable));
    }

    /** ✅ 회원 정보 수정 (POST 방식) */
    @PostMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId,
                                        @RequestBody UserUpdateRequestDto request,
                                        Authentication authentication) {
        verifyAdmin(authentication);
        adminUserService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }

    /** ✅ 회원 삭제 */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId,
                                        Authentication authentication) {
        verifyAdmin(authentication);
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    /** ✅ 비밀번호 초기화 */
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long userId,
                                           Authentication authentication) {
        verifyAdmin(authentication);
        adminUserService.resetPassword(userId);
        return ResponseEntity.ok().build();
    }

    /** ✅ 로그인 실패 횟수 초기화 */
    @PostMapping("/{userId}/reset-fail-count")
    public ResponseEntity<?> resetFailedLoginCount(@PathVariable Long userId,
                                                   Authentication authentication) {
        verifyAdmin(authentication);
        adminUserService.resetFailedLoginCount(userId);
        return ResponseEntity.ok().build();
    }

    // ✅ 검색 API 추가
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword,
                                         Authentication authentication) {
        verifyAdmin(authentication);
        List<UserEntity> users = adminUserService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }


    /** ✅ 관리자 권한 체크 */
    private void verifyAdmin(Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        if (!user.isAdmin()) {
            throw new SecurityException("관리자 권한이 필요합니다.");
        }
    }

    /** ✅ 예외 처리 */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }
}
