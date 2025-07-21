package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.musclebeaver.album.api.dto.PhotoAdminResponseDto;
import site.musclebeaver.album.api.repository.AdminPhotoRepository;

@Service
@RequiredArgsConstructor
public class AdminPhotoService {

    private final AdminPhotoRepository adminPhotoRepository;

    public Page<PhotoAdminResponseDto> getPagedPhotos(Pageable pageable, String searchType, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return adminPhotoRepository.findAllDtos(pageable);
        }
        switch (searchType) {
            case "username":
                return adminPhotoRepository.findByUsernameContainingDtos(keyword, pageable);
            case "folderName":
                return adminPhotoRepository.findByFolderNameContainingDtos(keyword, pageable);
            case "photoTitle":
                return adminPhotoRepository.findByPhotoTitleContainingDtos(keyword, pageable);
            default:
                throw new IllegalArgumentException("잘못된 searchType: " + searchType);
        }
    }

    public void deletePhoto(Long photoId) {
        adminPhotoRepository.deleteById(photoId);
    }
}
