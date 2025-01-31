package site.musclebeaver.album.api.repository;

import com.example.photoalbum.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 추가적인 쿼리 메서드가 필요하면 여기에 정의할 수 있습니다.
}