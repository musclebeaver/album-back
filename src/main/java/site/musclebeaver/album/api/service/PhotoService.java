package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.repository.FolderRepository;
import site.musclebeaver.album.api.repository.PhotoRepository;
import site.musclebeaver.album.api.entity.UserEntity;
import net.coobird.thumbnailator.Thumbnails;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final FolderRepository folderRepository;

    @Value("${album.upload-dir}")
    private String uploadDir;

    @Value("${album.access-url}")
    private String accessUrl;

    // ✅ 폴더의 사진 조회 (소유자 확인)
    public Page<Photo> getPhotosByFolderId(Long folderId, UserEntity user, Pageable pageable) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        return photoRepository.findByFolderAndFolderUser(folder, user, pageable); // ✅ 메서드명 변경
    }
    // ✅ 사진 업로드 (단건)
    public Photo savePhoto(String title, String description, Long folderId, MultipartFile file, UserEntity user) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 유저ID/폴더ID 디렉토리 생성
        File folderDir = new File(uploadDir + File.separator + user.getId() + File.separator + folder.getId());
        if (!folderDir.exists()) folderDir.mkdirs();

        // 파일명 생성
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = folderDir.getPath() + File.separator + fileName;
        file.transferTo(new File(filePath));

        // 접근 URL
        String imageUrl = accessUrl + user.getId() + "/" + folder.getId() + "/" + fileName;

        Photo photo = new Photo();
        photo.setTitle(title);
        photo.setDescription(description);
        photo.setImageUrl(imageUrl);
        photo.setFolder(folder);

        return photoRepository.save(photo);
    }



    // ✅ 사진 업로드 (다건) - 유효성 검사 추가
    public List<Photo> saveMultiplePhotos(Long folderId, List<MultipartFile> files, UserEntity user) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        File folderDir = new File(uploadDir + File.separator + user.getId() + File.separator + folder.getId());
        if (!folderDir.exists()) folderDir.mkdirs();

        List<Photo> savedPhotos = new ArrayList<>();

        for (MultipartFile file : files) {
            // ✅ 유효성 검사 (확장자 + MIME + 비어있는지)
            if (!isValidImageFile(file)) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다: " + file.getOriginalFilename());
            }

            // ✅ 용량 제한: 5MB 초과 금지
            if (file.getSize() > (5 * 1024 * 1024)) {
                throw new IllegalArgumentException("5MB 이하 파일만 업로드 가능합니다: " + file.getOriginalFilename());
            }

            // ✅ 확장자 제거
            String originalName = file.getOriginalFilename(); // 예: image.png
            String baseName = originalName != null ? originalName.substring(0, originalName.lastIndexOf(".")) : "image";
            String uuid = UUID.randomUUID().toString();
            String newFileName = uuid + "_" + baseName + ".jpg"; // ✅ 확장자 강제 .jpg

            String filePath = folderDir.getPath() + File.separator + newFileName;

            // ✅ 리사이징: 1024x1024 이하로 축소
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            Thumbnails.of(originalImage)
                    .size(1024, 1024)
                    .outputFormat("jpg") // 원본 형식 유지하려면 확장자 추출해서 처리
                    .toFile(filePath);

            // ✅ 이미지 접근 URL 구성
            String imageUrl = accessUrl + user.getId() + "/" + folder.getId() + "/" + newFileName;

            // ✅ DB 저장
            Photo photo = new Photo();
            photo.setTitle(originalName);
            photo.setDescription("Resized on upload");
            photo.setImageUrl(imageUrl);
            photo.setFolder(folder);

            savedPhotos.add(photoRepository.save(photo));
        }

        return savedPhotos;
    }

    // ✅ 사진 삭제
    public void deletePhoto(Long id, UserEntity user) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found with id: " + id));

        // 소유자 검증
        if (!photo.getFolder().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // ✅ 물리적 파일 경로 계산
        String imageUrl = photo.getImageUrl(); // 예: http://localhost:8081/img/11/43/xxx.jpg
        String relativePath = imageUrl.replace(accessUrl, ""); // 예: 11/43/xxx.jpg
        File file = new File(uploadDir, relativePath); // uploadDir + 11/43/xxx.jpg

        // ✅ 실제 파일 존재 시 삭제
        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.println("❌ 파일 삭제 실패: " + file.getAbsolutePath());
            }
        } else {
            System.err.println("⚠️ 파일이 존재하지 않음: " + file.getAbsolutePath());
        }

        // ✅ DB에서 레코드 삭제
        photoRepository.deleteById(id);
    }


    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;

        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();

        // 허용 확장자
        boolean hasValidExtension = originalName != null &&
                originalName.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$");

        // 허용 MIME 타입
        boolean hasValidMimeType = contentType != null &&
                contentType.toLowerCase().matches("image/(jpeg|png|gif|bmp|webp)");

        return hasValidExtension && hasValidMimeType;
    }
}
