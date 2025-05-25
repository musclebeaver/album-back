package site.musclebeaver.album.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.musclebeaver.album.api.entity.Folder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FolderResponseDto {
    private Long id;
    private String name;

    // 엔티티 → DTO 변환용 생성자
    public FolderResponseDto(Folder folder) {
        this.id = folder.getId();
        this.name = folder.getName();
    }
}