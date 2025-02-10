package site.musclebeaver.album.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.service.PhotoService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    // 사진 업로드 (페도라 서버에 저장)
    @PostMapping("/upload")
    public ResponseEntity<Photo> uploadPhoto(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Long folderId,
            @RequestParam MultipartFile file) {
        try {
            Photo photo = photoService.savePhoto(title, description, folderId, file);
            return ResponseEntity.ok(photo);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
    //  대량 사진 업로드
    @PostMapping("/upload/multiple")
    public ResponseEntity<List<Photo>> uploadMultiplePhotos(
            @RequestParam Long folderId,
            @RequestParam List<MultipartFile> files) {
        try {
            if (files.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            List<Photo> photos = photoService.saveMultiplePhotos(folderId, files);
            return ResponseEntity.ok(photos);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    //  특정 폴더의 모든 사진 조회
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<Photo>> getPhotosByFolderId(@PathVariable Long folderId) {
        List<Photo> photos = photoService.getPhotosByFolderId(folderId);
        return ResponseEntity.ok(photos);
    }

    //  모든 사진 조회
    @GetMapping("/all")
    public ResponseEntity<List<Photo>> getAllPhotos() {
        return ResponseEntity.ok(photoService.getAllPhotos());
    }

    //  특정 사진 조회
    @GetMapping("/{id}")
    public ResponseEntity<Photo> getPhotoById(@PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoById(id));
    }

    //  사진 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePhoto(@PathVariable Long id) {
        try {
            photoService.deletePhoto(id);
            return ResponseEntity.ok("Photo deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
