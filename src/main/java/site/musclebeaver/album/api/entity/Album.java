package site.musclebeaver.album.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "photo") // 테이블 이름을 명시적으로 지정 (선택 사항)
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100) // 제목은 null 불가, 최대 100자
    private String title;

    @Column(length = 500) // 설명은 최대 500자
    private String description;

    @Column(nullable = false) // 이미지 URL은 null 불가
    private String imageUrl;
}