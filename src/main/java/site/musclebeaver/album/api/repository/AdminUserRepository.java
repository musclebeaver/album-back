package site.musclebeaver.album.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.musclebeaver.album.api.entity.UserEntity;

public interface AdminUserRepository extends JpaRepository<UserEntity, Long> {
    Page<UserEntity> findByUsernameContaining(String username, Pageable pageable);
    Page<UserEntity> findByEmailContaining(String email, Pageable pageable);
    Page<UserEntity> findByUsernameContainingOrEmailContaining(String username, String email, Pageable pageable);
}
