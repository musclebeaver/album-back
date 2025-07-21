package site.musclebeaver.album.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.musclebeaver.album.api.entity.Photo;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoAdminResponseDto {
    private Long photoId;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;

    private Long folderId;
    private String folderName;

    private Long userId;
    private String username;

    public PhotoAdminResponseDto(Photo photo) {
        this.photoId = photo.getId();
        this.title = photo.getTitle();
        this.description = photo.getDescription();
        this.imageUrl = photo.getImageUrl();
        this.createdAt = photo.getCreatedAt();

        if (photo.getFolder() != null) {
            this.folderId = photo.getFolder().getId();
            this.folderName = photo.getFolder().getName();

            if (photo.getFolder().getUser() != null) {
                this.userId = photo.getFolder().getUser().getId();
                this.username = photo.getFolder().getUser().getUsername();
            }
        }
    }
}
