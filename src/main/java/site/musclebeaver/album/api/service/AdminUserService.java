package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.musclebeaver.album.api.dto.UserResponseDto;
import site.musclebeaver.album.api.dto.UserUpdateRequestDto;
import site.musclebeaver.album.api.repository.AdminUserRepository;
import site.musclebeaver.album.api.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getPagedUsers(Pageable pageable, String searchType, String keyword) {
        Page<UserEntity> users;

        if (keyword == null || keyword.isBlank()) {
            users = adminUserRepository.findAll(pageable);
        } else {
            switch (searchType) {
                case "name":
                    users = adminUserRepository.findByUsernameContaining(keyword, pageable);
                    break;
                case "email":
                    users = adminUserRepository.findByEmailContaining(keyword, pageable);
                    break;
                default:
                    users = adminUserRepository.findByUsernameContainingOrEmailContaining(keyword, keyword, pageable);
                    break;
            }
        }

        return users.map(this::convertToDto);
    }
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return convertToDto(user);
    }

    @Transactional
    public void updateUser(Long userId, UserUpdateRequestDto request) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 필요한 필드만 갱신
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getIsApproved() != null) {
            user.setApproved(request.getIsApproved());
        }
        if (request.getIsAdmin() != null) {
            user.setAdmin(request.getIsAdmin());
        }
        // ✅ Password는 건드리지 않음
    }
    @Transactional
    public void deleteUser(Long userId) {
        adminUserRepository.deleteById(userId);
    }

    @Transactional
    public void resetPassword(Long userId) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        String initialPassword = "123456";
        user.setPassword(passwordEncoder.encode(initialPassword)); // ✅ 암호화
    }

    @Transactional
    public void resetFailedLoginCount(Long userId) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        user.setFailedLoginCount(0);
    }

    private UserResponseDto convertToDto(UserEntity user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isApproved(),
                user.isAdmin(),
                user.getFailedLoginCount()
        );
    }
}
