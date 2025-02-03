package site.musclebeaver.album.login.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")  // DB 테이블과 매핑
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가 ID
    private Long id;

    @Column(nullable = false, unique = true)  // 사용자명은 고유해야 함
    private String username;

    @Column(nullable = false, unique = true)  // 이메일도 고유해야 함
    private String email;

    @Column(nullable = false)  // 비밀번호는 필수
    private String password;

    @Column(nullable = false)  // 관리자 승인 여부
    private boolean isApproved = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> folders = new ArrayList<>(); // UserEntity와 Folder의 양방향 관계
}
