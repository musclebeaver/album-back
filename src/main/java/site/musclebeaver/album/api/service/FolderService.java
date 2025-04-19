package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.exception.FolderAlreadyExistsException;
import site.musclebeaver.album.api.repository.FolderRepository;
import site.musclebeaver.album.api.repository.PhotoRepository;
import org.springframework.stereotype.Service;
import site.musclebeaver.album.user.entity.UserEntity;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final PhotoRepository photoRepository;
    // 페도라 서버에 저장할 경로
    private final String UPLOAD_DIR = "/img/uploads/";

    // 폴더 생성 (특정 사용자용)
    public Folder createFolder(String name, UserEntity user) {
        // 동일한 사용자 내에서 폴더 이름 중복 검사
        if (folderRepository.existsByNameAndUser(name, user)) {
            throw new FolderAlreadyExistsException("Folder name already exists for this user");
        }

        Folder folder = new Folder();
        folder.setName(name);
        folder.setUser(user);
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

    // 폴더 삭제
    public void deleteFolder(Long folderId) {
         //  폴더 조회
        Optional<Folder> folderOpt = folderRepository.findById(folderId);
        if (folderOpt.isEmpty()) {
            throw new IllegalArgumentException("Folder not found with id: " + folderId);
        }
        Folder folder = folderOpt.get();

        //  해당 폴더 내 모든 사진 조회
        List<Photo> photos = photoRepository.findByFolder_Id(folderId);

        //  사진 파일 삭제
        for (Photo photo : photos) {
            String imagePath = UPLOAD_DIR + photo.getImageUrl().replace("/img/uploads/", ""); // 파일 경로 추출
            File file = new File(imagePath);
            if (file.exists()) {
                file.delete(); // 실제 파일 삭제
            }
        }

        // DB에서 해당 폴더의 모든 사진 삭제
        photoRepository.deleteAll(photos);

        //  폴더 삭제
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

    //소유자 검증 메서드
    public Folder getFolderIfOwnedByUser(Long folderId, UserEntity user) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new SecurityException("해당 폴더에 대한 권한이 없습니다.");
        }

        return folder;
    }


}