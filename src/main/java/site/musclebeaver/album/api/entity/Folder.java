package site.musclebeaver.album.api.entity;

import jakarta.persistence.*;
import lombok.*;
import site.musclebeaver.album.login.entity.UserEntity; // UserEntity import 추가
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "folder")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100) // 폴더 이름은 null 불가, 최대 100자
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // UserEntity와의 관계, null 불가
    private UserEntity user;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();
}