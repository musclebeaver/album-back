package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.musclebeaver.album.api.dto.UserUpdateRequestDto;
import site.musclebeaver.album.user.entity.UserEntity;
import site.musclebeaver.album.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateUser(Long userId, UserUpdateRequestDto request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        if (request.getIsApproved() != null) {
            user.setIsApproved(request.getIsApproved());
        }

        if (request.getIsAdmin() != null) {
            user.setIsAdmin(request.getIsAdmin());
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public void resetPassword(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        user.setPassword("initialPassword"); // ⚠️ 실제 구현에서는 암호화 필요
    }

    @Transactional
    public void resetFailedLoginCount(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        user.setFailedLoginCount(0);
    }
}
