package site.musclebeaver.album.login.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {
    private String username;
    private String email;
    private String password;
}
