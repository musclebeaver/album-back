package site.musclebeaver.album.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import site.musclebeaver.album.api.entity.UserEntity;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final UserEntity user;

    public CustomUserDetails(UserEntity user) {
        this.user = user;
    }

    public UserEntity getUserEntity() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> "ROLE_USER");
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername(); // 또는 email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
