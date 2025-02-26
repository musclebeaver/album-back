package site.musclebeaver.album.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.musclebeaver.album.user.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 사용자명을 통해 UserEntity 조회
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);

    // ✅ 이메일 중복 여부 확인 메서드 추가
    boolean existsByEmail(String email);
}
