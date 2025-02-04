package site.musclebeaver.album.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.musclebeaver.album.api.entity.Photo;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 특정 폴더 ID로 사진 리스트 조회
    List<Photo> findByFolder_Id(Long folderId);
}
