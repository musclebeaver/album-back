package site.musclebeaver.album.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.api.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


        @Override
        public UserDetails loadUserByUsername(String username) {
            UserEntity userEntity = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
            System.out.println("로그인 시도 username=" + username);
            return new CustomUserDetails(userEntity);
        }
}
