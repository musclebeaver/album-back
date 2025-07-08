package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.musclebeaver.album.api.dto.FolderAdminResponseDto;
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

    public void updateFolderName(Long folderId, String newName) {
        throw new UnsupportedOperationException("폴더 이름 변경 로직은 DTO 쿼리와 별도로 구현해야 합니다.");
    }

    public void deleteFolder(Long folderId) {
        adminFolderRepository.deleteById(folderId);
    }
}
