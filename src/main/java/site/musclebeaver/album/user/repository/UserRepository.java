package site.musclebeaver.album.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.musclebeaver.album.user.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // ✅ RefreshToken으로 사용자 조회
    Optional<UserEntity> findByRefreshToken(String refreshToken);
}
