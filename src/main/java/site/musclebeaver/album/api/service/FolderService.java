package site.musclebeaver.album.api.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.api.repository.FolderRepository;
import site.musclebeaver.album.api.repository.PhotoRepository;
import site.musclebeaver.album.exception.FolderAlreadyExistsException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final PhotoRepository photoRepository;

    // ✅ S3 템플릿 추가
    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${album.access-url}")
    private String accessUrl;

    // 폴더 생성
    public Folder createFolder(String name, UserEntity user) {
        if (folderRepository.existsByNameAndUser(name, user)) {
            throw new FolderAlreadyExistsException("Folder name already exists for this user");
        }

        Folder folder = new Folder();
        folder.setName(name);
        folder.setUser(user);

        // ✅ 로컬 디렉토리(mkdir) 생성 로직 삭제됨 (S3는 폴더 생성이 필요 없음)

        return folderRepository.save(folder);
    }

    // 특정 사용자의 폴더 목록 조회
    public List<Folder> getFoldersByUserId(Long userId) {
        return folderRepository.findByUser_Id(userId);
    }

    public List<Folder> getFoldersByUser(UserEntity user) {
        return folderRepository.findByUser(user);
    }

    // 폴더 조회 (ID로)
    public Folder getFolderById(Long id) {
        return folderRepository.findById(id).orElse(null);
    }

    // ✅ 폴더 삭제 - S3 파일 삭제 적용
    public void deleteFolder(Long folderId) {
        // 폴더 조회
        Optional<Folder> folderOpt = folderRepository.findById(folderId);
        if (folderOpt.isEmpty()) {
            throw new IllegalArgumentException("Folder not found with id: " + folderId);
        }
        Folder folder = folderOpt.get();

        // 해당 폴더 내 모든 사진 조회
        List<Photo> photos = photoRepository.findByFolderId(folderId);

        // ✅ S3에서 사진 파일들 삭제
        for (Photo photo : photos) {
            String imageUrl = photo.getImageUrl();
            // URL에서 Key 추출 (예: https://.../1/10/file.jpg -> 1/10/file.jpg)
            if (imageUrl != null && imageUrl.startsWith(accessUrl)) {
                String s3Key = imageUrl.replace(accessUrl, "");
                s3Template.deleteObject(bucketName, s3Key);
            }
        }

        // DB에서 해당 폴더의 모든 사진 삭제
        photoRepository.deleteAll(photos);

        // 폴더 삭제
        folderRepository.delete(folder);
    }

    // 폴더 이름 변경
    public Folder updateFolderName(Long id, String newName) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (folderRepository.existsByNameAndUser(newName, folder.getUser())) {
            throw new FolderAlreadyExistsException("Folder name already exists for this user.");
        }

        folder.setName(newName);
        return folderRepository.save(folder);
    }

    // 소유자 검증 메서드
    public Folder getFolderIfOwnedByUser(Long folderId, UserEntity user) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new SecurityException("해당 폴더에 대한 권한이 없습니다.");
        }

        return folder;
    }
}