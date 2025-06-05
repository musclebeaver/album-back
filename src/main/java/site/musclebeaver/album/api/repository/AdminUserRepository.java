package site.musclebeaver.album.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.musclebeaver.album.user.entity.UserEntity;

public interface AdminUserRepository extends JpaRepository<UserEntity, Long> {
      // 아이디 또는 이메일을 통한 부분 검색
    List<UserEntity> findByUsernameContainingOrEmailContaining(String username, String email);

}
