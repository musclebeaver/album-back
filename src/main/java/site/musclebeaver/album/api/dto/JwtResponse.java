package site.musclebeaver.album.api.dto;

public class JwtResponse {
    private String token;
    private Long userId;
    private boolean approved;
    private boolean isAdmin; // ✅ 관리자 여부

    public JwtResponse(String token, Long userId, boolean approved, boolean isAdmin) {
        this.token = token;
        this.userId = userId;
        this.approved = approved;
        this.isAdmin = isAdmin;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
