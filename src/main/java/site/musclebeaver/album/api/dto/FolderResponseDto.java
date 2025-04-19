package site.musclebeaver.album.api.dto;

import site.musclebeaver.album.api.entity.Folder;

public class FolderResponseDto {
    private Long id;
    private String name;

    public FolderResponseDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // 엔티티 → DTO 변환용 생성자
    public FolderResponseDto(Folder folder) {
        this.id = folder.getId();
        this.name = folder.getName();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
