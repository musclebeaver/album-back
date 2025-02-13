package site.musclebeaver.album.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.musclebeaver.album.login.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 사용자명을 통해 UserEntity 조회
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
