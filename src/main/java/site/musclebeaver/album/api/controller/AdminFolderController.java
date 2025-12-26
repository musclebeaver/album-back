package site.musclebeaver.album.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.api.dto.FolderRequestDto;
import site.musclebeaver.album.api.service.AdminFolderService;
import site.musclebeaver.album.api.service.UserService;
import site.musclebeaver.album.api.entity.UserEntity;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/folders")
@RequiredArgsConstructor
public class AdminFolderController {

    private final AdminFolderService adminFolderService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getFolders(@PageableDefault(size = 20) Pageable pageable,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String searchType,
                                        Authentication authentication) {
        verifyAdmin(authentication);
        return ResponseEntity.ok(adminFolderService.getPagedFolders(pageable, searchType, keyword));
    }

    @PutMapping("/{folderId}/name")
    public ResponseEntity<?> updateFolderName(@PathVariable Long folderId,
                                              @RequestBody FolderRequestDto request,
                                              Authentication authentication) {
        verifyAdmin(authentication);
        adminFolderService.updateFolderName(folderId, request.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable Long folderId,
                                          Authentication authentication) {
        verifyAdmin(authentication);
        adminFolderService.deleteFolder(folderId);
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
