package site.musclebeaver.album.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.entity.UserEntity;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 특정 폴더 ID로 사진 리스트 조회
    List<Photo> findByFolderId(Long folderId);
    Page<Photo> findByFolderAndUser(Folder folder, UserEntity user, Pageable pageable);
}
