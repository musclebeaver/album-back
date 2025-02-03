package site.musclebeaver.album.api.service;

import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.exception.FolderAlreadyExistsException;
import site.musclebeaver.album.api.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

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
    public List<Folder> getFoldersByUser(UserEntity user) {
        return folderRepository.findByUser(user);
    }

    // 폴더 조회 (ID로)
    public Folder getFolderById(Long id) {
        return folderRepository.findById(id).orElse(null);
    }

    // 폴더 삭제
    public void deleteFolder(Long id) {
        folderRepository.deleteById(id);
    }

    // 폴더 이름 변경
    public Folder updateFolderName(Long id, String newName) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (folderRepository.existsByName(newName)) {
            throw new FolderAlreadyExistsException("Folder name already exists");
        }

        folder.setName(newName);
        return folderRepository.save(folder);
    }
}