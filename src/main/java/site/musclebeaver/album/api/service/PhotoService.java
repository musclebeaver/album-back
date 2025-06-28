package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.repository.FolderRepository;
import site.musclebeaver.album.api.repository.PhotoRepository;
import site.musclebeaver.album.api.entity.UserEntity;

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
    public List<Photo> getPhotosByFolderId(Long folderId, UserEntity user) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        return photoRepository.findByFolder_Id(folderId);
    }

    // ✅ 사진 업로드 (단건)
    public Photo savePhoto(String title, String description, Long folderId, MultipartFile file, UserEntity user) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 사용자별 디렉토리
        File userDir = new File(uploadDir + File.separator + user.getId());
        if (!userDir.exists()) userDir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = userDir.getPath() + File.separator + fileName;
        file.transferTo(new File(filePath));

        // 사용자별 접근 경로
        String imageUrl = accessUrl + user.getId() + "/" + fileName;

        Photo photo = new Photo();
        photo.setTitle(title);
        photo.setDescription(description);
        photo.setImageUrl(imageUrl);
        photo.setFolder(folder);

        return photoRepository.save(photo);
    }

    // ✅ 사진 업로드 (다건)
    public List<Photo> saveMultiplePhotos(Long folderId, List<MultipartFile> files, UserEntity user) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        File userDir = new File(uploadDir + File.separator + user.getId());
        if (!userDir.exists()) userDir.mkdirs();

        List<Photo> savedPhotos = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String filePath = userDir.getPath() + File.separator + fileName;
            file.transferTo(new File(filePath));

            String imageUrl = accessUrl + user.getId() + "/" + fileName;

            Photo photo = new Photo();
            photo.setTitle(file.getOriginalFilename());
            photo.setDescription("Uploaded via bulk upload");
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

        if (!photo.getFolder().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 이미지 경로에서 userId 포함된 상대 경로 추출
        String relativePath = photo.getImageUrl().replace(accessUrl, "");
        File file = new File(uploadDir + File.separator + relativePath);

        if (file.exists()) {
            file.delete();
        }

        photoRepository.deleteById(id);
    }
}
