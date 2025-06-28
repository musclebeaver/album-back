package site.musclebeaver.album.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import site.musclebeaver.album.api.dto.FolderRequestDto;
import site.musclebeaver.album.api.dto.FolderResponseDto;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.exception.FolderAlreadyExistsException;
import site.musclebeaver.album.api.service.FolderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.api.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/folders")
public class FolderController {

    private final FolderService folderService;
    private final UserService userService;

    @Autowired
    public FolderController(FolderService folderService, UserService userService) {
        this.folderService = folderService;
        this.userService = userService;

    }

    // 테스트용 엔드포인트 추가
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Success");
    }


    @GetMapping("/my")
    public List<FolderResponseDto> getMyFolders(Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        List<Folder> folders = folderService.getFoldersByUser(user);
        return folders.stream()
                .map(FolderResponseDto::new)
                .collect(Collectors.toList());
    }
    // 특정 사용자의 폴더 생성
    @PostMapping("/create")
    public ResponseEntity<?> createFolder(@RequestBody FolderRequestDto request, Authentication authentication) {
        System.out.println("▶ 폴더 생성 진입");

        String username = authentication.getName();
        System.out.println("▶ username: " + username);

        String folderName = request.getName();
        System.out.println("▶ folderName: " + folderName);

        UserEntity user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));


        Folder folder = folderService.createFolder(request.getName(), user);
        return ResponseEntity.ok(new FolderResponseDto(folder)); // ✅ DTO로 변환
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
    public ResponseEntity<String> deleteFolder(@PathVariable Long folderId, Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Folder folder = folderService.getFolderIfOwnedByUser(folderId, user);
        folderService.deleteFolder(folder.getId());

        return ResponseEntity.ok("폴더 및 사진이 성공적으로 삭제되었습니다.");
    }
    // 폴더 이름 변경

    @PutMapping("/{id}/name")
    public Folder updateFolderName(@PathVariable Long id,
                                   @RequestBody FolderRequestDto dto,
                                   Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Folder folder = folderService.getFolderIfOwnedByUser(id, user);
        return folderService.updateFolderName(folder.getId(), dto.getName());
    }
    // 이미 존재하는 폴더 예외 처리
    @ExceptionHandler(FolderAlreadyExistsException.class)
    public ResponseEntity<String> handleFolderAlreadyExists(FolderAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    //권한 관련 예외 처리
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}