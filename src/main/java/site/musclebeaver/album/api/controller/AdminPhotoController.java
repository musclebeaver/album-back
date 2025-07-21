package site.musclebeaver.album.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.api.service.AdminPhotoService;
import site.musclebeaver.album.api.service.UserService;
import site.musclebeaver.album.api.entity.UserEntity;

@RestController
@RequestMapping("/admin/photos")
@RequiredArgsConstructor
public class AdminPhotoController {

    private final AdminPhotoService adminPhotoService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getPhotos(@PageableDefault(size = 20) Pageable pageable,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String searchType,
                                       Authentication authentication) {
        verifyAdmin(authentication);
        return ResponseEntity.ok(adminPhotoService.getPagedPhotos(pageable, searchType, keyword));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable Long photoId, Authentication authentication) {
        verifyAdmin(authentication);
        adminPhotoService.deletePhoto(photoId);
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