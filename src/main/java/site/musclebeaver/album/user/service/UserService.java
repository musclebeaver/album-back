package site.musclebeaver.album.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import site.musclebeaver.album.user.dto.SignUpRequestDto;
import site.musclebeaver.album.user.entity.UserEntity;
import site.musclebeaver.album.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 모든 사용자 조회
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    // 사용자 저장 (회원가입 시 비밀번호 암호화)
    public UserEntity save(UserEntity user) {
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);

    }

    // 사용자 ID로 사용자 조회 (추가)
    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }


    // 사용자 이름으로 사용자 찾기
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);

    }

    // 회원가입 처리
    public UserEntity registerUser(SignUpRequestDto signUpRequest) {
        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken.");
        }
        UserEntity user = new UserEntity();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        return userRepository.save(user);

    }

        // ✅ username 중복 확인 메서드 추가
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // ✅ email 중복 확인 메서드 추가
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}