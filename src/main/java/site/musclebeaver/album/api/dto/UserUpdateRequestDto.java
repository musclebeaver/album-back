package site.musclebeaver.album.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {
    private Boolean isApproved;
    private Boolean isAdmin;
}