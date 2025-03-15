package site.musclebeaver.album.user.dto;

public class JwtResponse {
    private String token;
    private Long userId; // ✅ 사용자 ID 추가

    public JwtResponse(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }
}
