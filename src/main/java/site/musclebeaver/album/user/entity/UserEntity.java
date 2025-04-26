package site.musclebeaver.album.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.musclebeaver.album.api.entity.Folder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")  // DB 테이블과 매핑
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isApproved = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> folders = new ArrayList<>();

    @Column(nullable = false)
    private int failedLoginCount = 0;

    @Column(nullable = false)
    private boolean isAdmin = false;

    // ✅ Refresh Token 추가
    @Column(name = "refresh_token")
    private String refreshToken;
}
