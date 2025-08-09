package site.musclebeaver.album.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.musclebeaver.album.api.dto.SignUpRequestDto;
import site.musclebeaver.album.api.entity.UserEntity;
import site.musclebeaver.album.api.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // ✅ 이렇게 바꿔야 함
    @Autowired
    private PasswordEncoder passwordEncoder;

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

    public void increaseFailedLoginCount(UserEntity user) {
        user.setFailedLoginCount(user.getFailedLoginCount() + 1);
        userRepository.save(user);
    }

    public void resetFailedLoginCount(UserEntity user) {
        user.setFailedLoginCount(0);
        userRepository.save(user);
    }
    // ✅ RefreshToken 저장
    public void updateRefreshToken(String username, String refreshToken) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        });
    }

    // ✅ RefreshToken으로 사용자 조회
    public Optional<UserEntity> findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 간단 유효성 검사 (원하면 더 강화 가능)
//        if (newPassword == null || newPassword.length() < 6) {
//            throw new IllegalArgumentException("새 비밀번호는 6자 이상이어야 합니다.");
//        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호가 기존 비밀번호와 동일합니다.");
        }
        // 공백 방지 처리 (선택)
        String trimmedNewPw = newPassword.trim();

        // 새 비밀번호 암호화 후 저장
        String encoded = passwordEncoder.encode(trimmedNewPw);
        user.setPassword(encoded);
        user.setRefreshToken(null); // ✅ 변경 시 토큰 무효화
        userRepository.save(user);

        // ✅ 디버그: 저장된 해시와 새 비밀번호 매칭 검사
        boolean matchAfterSave = passwordEncoder.matches(trimmedNewPw, user.getPassword());
        System.out.println("[CHANGE PW DEBUG] userId=" + userId
                + ", newPwRaw=" + trimmedNewPw
                + ", matchAfterSave=" + matchAfterSave);
    }

    @Transactional
    public void invalidateRefreshToken(String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> user.setRefreshToken(null));
    }

}