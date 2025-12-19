package site.musclebeaver.album.api.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.api.repository.FolderRepository;
import site.musclebeaver.album.api.repository.PhotoRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final FolderRepository folderRepository;

    // ✅ S3 작업을 도와주는 템플릿 주입
    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${album.access-url}")
    private String accessUrl;

    // 폴더의 사진 조회 (기존 유지)
    public Page<Photo> getPhotosByFolderId(Long folderId, UserEntity user, Pageable pageable) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        return photoRepository.findByFolderAndFolderUser(folder, user, pageable);
    }

    // ✅ 사진 업로드 (단건) - S3 적용
    public Photo savePhoto(String title, String description, Long folderId, MultipartFile file, UserEntity user) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 1. S3에 저장될 파일 Key(경로+파일명) 생성
        // 예: 1/10/uuid_filename.jpg (유저ID/폴더ID/파일명)
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String s3Key = user.getId() + "/" + folder.getId() + "/" + uuid + "_" + originalFilename;

        // 2. S3 업로드
        try (InputStream inputStream = file.getInputStream()) {
            s3Template.upload(bucketName, s3Key, inputStream);
        }

        // 3. 접근 URL 구성
        // accessUrl이 "https://cloudfront.net/" 이라면 -> https://cloudfront.net/1/10/xxxx.jpg
        String imageUrl = accessUrl + s3Key;

        Photo photo = new Photo();
        photo.setTitle(title);
        photo.setDescription(description);
        photo.setImageUrl(imageUrl);
        photo.setFolder(folder);

        return photoRepository.save(photo);
    }

    // ✅ 사진 업로드 (다건) - S3 + 리사이징 적용
    public List<Photo> saveMultiplePhotos(Long folderId, List<MultipartFile> files, UserEntity user) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        List<Photo> savedPhotos = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!isValidImageFile(file)) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다: " + file.getOriginalFilename());
            }

            if (file.getSize() > (5 * 1024 * 1024)) {
                throw new IllegalArgumentException("5MB 이하 파일만 업로드 가능합니다: " + file.getOriginalFilename());
            }

            String originalName = file.getOriginalFilename();
            String baseName = originalName != null ? originalName.substring(0, originalName.lastIndexOf(".")) : "image";
            String uuid = UUID.randomUUID().toString();

            // 확장자 강제 .jpg (리사이징 후 jpg로 변환하므로)
            String s3Key = user.getId() + "/" + folder.getId() + "/" + uuid + "_" + baseName + ".jpg";

            // ✅ 리사이징 로직 변경 (File -> ByteArrayOutputStream)
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            Thumbnails.of(originalImage)
                    .size(1024, 1024)
                    .outputFormat("jpg")
                    .toOutputStream(os); // 메모리에 씀

            // ✅ S3 업로드
            try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
                s3Template.upload(bucketName, s3Key, is);
            }

            String imageUrl = accessUrl + s3Key;

            Photo photo = new Photo();
            photo.setTitle(originalName);
            photo.setDescription("Resized on upload");
            photo.setImageUrl(imageUrl);
            photo.setFolder(folder);

            savedPhotos.add(photoRepository.save(photo));
        }

        return savedPhotos;
    }

    // ✅ 사진 삭제 - S3 적용
    public void deletePhoto(Long id, UserEntity user) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found with id: " + id));

        if (!photo.getFolder().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 1. URL에서 S3 Key 추출
        // DB에 저장된 URL: https://내버킷.../1/10/xxx.jpg
        // accessUrl: https://내버킷.../
        // 결과 s3Key: 1/10/xxx.jpg
        String imageUrl = photo.getImageUrl();
        String s3Key = imageUrl.replace(accessUrl, "");

        // 2. S3에서 파일 삭제
        s3Template.deleteObject(bucketName, s3Key);

        // 3. DB 삭제
        photoRepository.deleteById(id);
    }

    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();
        boolean hasValidExtension = originalName != null &&
                originalName.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$");
        boolean hasValidMimeType = contentType != null &&
                contentType.toLowerCase().matches("image/(jpeg|png|gif|bmp|webp)");
        return hasValidExtension && hasValidMimeType;
    }
}