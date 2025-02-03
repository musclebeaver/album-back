package site.musclebeaver.album.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.musclebeaver.album.api.entity.Folder;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    // userId만으로 폴더 리스트 조회 (더 효율적인 방법)
    List<Folder> findByUser_Id(Long userId);
}
