package site.musclebeaver.album.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private boolean isApproved;
    private boolean isAdmin;
    private int failedLoginCount;
}
