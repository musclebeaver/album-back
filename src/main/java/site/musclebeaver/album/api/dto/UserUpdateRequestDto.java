package site.musclebeaver.album.api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    private String username;
    private String email;
    private String nickname;
    private Boolean enabled;
    private Boolean isApproved;
    private Boolean isAdmin;
}