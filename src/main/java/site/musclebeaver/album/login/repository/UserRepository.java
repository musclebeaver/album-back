package site.musclebeaver.album.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.musclebeaver.album.login.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
// 추가적인 쿼리 메소드 정의 가능
}