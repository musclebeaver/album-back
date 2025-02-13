package site.musclebeaver.album.api.controller;

import lombok.RequiredArgsConstructor;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.exception.FolderAlreadyExistsException;
import site.musclebeaver.album.api.service.FolderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.user.entity.UserEntity;
import site.musclebeaver.album.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private FolderService folderService;
    private UserService userService;
    // 특정 사용자의 폴더 생성
    @PostMapping("/create")
    public Folder createFolder(@RequestParam String name, @RequestParam Long userId) {
        UserEntity user = userService.getUserById(userId); // UserService는 별도로 구현 필요
        return folderService.createFolder(name, user);
    }

    // 특정 사용자의 폴더 목록 조회
    @GetMapping("/user/{userId}")
    public List<Folder> getFoldersByUser(@PathVariable Long userId) {
        // userId를 사용하여 UserEntity를 조회 (예: UserService를 통해)
        UserEntity user = userService.getUserById(userId); // UserService는 별도로 구현 필요
        return folderService.getFoldersByUser(user);
    }


    // 폴더 삭제
    @DeleteMapping("/{folderId}")
    public ResponseEntity<String> deleteFolder(@PathVariable Long folderId) {
        try {
            folderService.deleteFolder(folderId);
            return ResponseEntity.ok("Folder and all photos deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 폴더 이름 변경
    @PutMapping("/{id}/name")
    public Folder updateFolderName(@PathVariable Long id, @RequestParam String newName) {
        return folderService.updateFolderName(id, newName);
    }

    // 예외 처리
    @ExceptionHandler(FolderAlreadyExistsException.class)
    public ResponseEntity<String> handleFolderAlreadyExists(FolderAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}