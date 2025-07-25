package site.musclebeaver.album.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.musclebeaver.album.api.dto.PhotoResponseDto;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.service.PhotoService;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.security.CustomUserDetails;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping("/upload")
    public ResponseEntity<Photo> uploadPhoto(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Long folderId,
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            UserEntity user = userDetails.getUserEntity();
            Photo photo = photoService.savePhoto(title, description, folderId, file, user);
            return ResponseEntity.ok(photo);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<PhotoResponseDto>> uploadMultiplePhotos(
            @RequestParam Long folderId,
            @RequestParam List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            UserEntity user = userDetails.getUserEntity();
            List<Photo> photos = photoService.saveMultiplePhotos(folderId, files, user);
            List<PhotoResponseDto> dtos = photos.stream()
                    .map(PhotoResponseDto::from)
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // ✅ 수정: 페이지 기반 사진 가져오기
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<PhotoResponseDto>> getPhotosByFolderId(
            @PathVariable Long folderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserEntity user = userDetails.getUserEntity();

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Photo> photoPage = photoService.getPhotosByFolderId(folderId, user, pageRequest);

        List<PhotoResponseDto> response = photoPage.getContent().stream()
                .map(PhotoResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePhoto(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            UserEntity user = userDetails.getUserEntity();
            photoService.deletePhoto(id, user);
            return ResponseEntity.ok("Photo deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
