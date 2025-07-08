package site.musclebeaver.album.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FolderAdminResponseDto {
    private Long id;
    private String name;
    private Long userId;
    private String username;
    private long photoCount;
}
