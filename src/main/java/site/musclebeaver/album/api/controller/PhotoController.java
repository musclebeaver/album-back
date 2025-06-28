package site.musclebeaver.album.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.service.PhotoService;
import site.musclebeaver.album.api.entity.UserEntity;

import java.io.IOException;
import java.util.List;

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
            @AuthenticationPrincipal UserEntity user) {
        try {
            Photo photo = photoService.savePhoto(title, description, folderId, file, user);
            return ResponseEntity.ok(photo);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<Photo>> uploadMultiplePhotos(
            @RequestParam Long folderId,
            @RequestParam List<MultipartFile> files,
            @AuthenticationPrincipal UserEntity user) {
        try {
            List<Photo> photos = photoService.saveMultiplePhotos(folderId, files, user);
            return ResponseEntity.ok(photos);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<Photo>> getPhotosByFolderId(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserEntity user) {
        List<Photo> photos = photoService.getPhotosByFolderId(folderId, user);
        return ResponseEntity.ok(photos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePhoto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserEntity user) {
        try {
            photoService.deletePhoto(id, user);
            return ResponseEntity.ok("Photo deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
