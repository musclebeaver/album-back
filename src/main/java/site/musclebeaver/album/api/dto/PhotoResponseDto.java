package site.musclebeaver.album.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.musclebeaver.album.api.entity.Photo;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponseDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static PhotoResponseDto from(Photo photo) {
        PhotoResponseDto dto = new PhotoResponseDto();
        dto.id = photo.getId();
        dto.title = photo.getTitle();
        dto.description = photo.getDescription();
        dto.imageUrl = photo.getImageUrl();
        dto.createdAt = photo.getCreatedAt();
        return dto;
    }

    // getter, setter 생략 가능 (롬복 @Data 써도 됨)
}
