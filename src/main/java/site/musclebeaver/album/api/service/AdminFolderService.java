package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.musclebeaver.album.api.dto.FolderAdminResponseDto;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.repository.AdminFolderRepository;

@Service
@RequiredArgsConstructor
public class AdminFolderService {

    private final AdminFolderRepository adminFolderRepository;

    public Page<FolderAdminResponseDto> getPagedFolders(Pageable pageable, String searchType, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return adminFolderRepository.findAllDtos(pageable);
        }
        switch (searchType) {
            case "username":
                return adminFolderRepository.findByUsernameContainingDtos(keyword, pageable);
            case "folderName":
                return adminFolderRepository.findByFolderNameContainingDtos(keyword, pageable);
            default:
                throw new IllegalArgumentException("잘못된 searchType: " + searchType);
        }
    }

    // 폴더명 변경
    public void updateFolderName(Long folderId, String name) {
        Folder folder = adminFolderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        folder.setName(name);
        adminFolderRepository.save(folder); // 변경 감지 or save 명시적으로
    }

    public void deleteFolder(Long folderId) {
        adminFolderRepository.deleteById(folderId);
    }
}
