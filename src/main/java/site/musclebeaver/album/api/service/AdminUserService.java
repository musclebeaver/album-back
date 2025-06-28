package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setApproved(request.isApproved());
        user.setAdmin(request.isAdmin());
    }
    @Transactional
    public void deleteUser(Long userId) {
        adminUserRepository.deleteById(userId);
    }

    @Transactional
    public void resetPassword(Long userId) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        user.setPassword("초기비밀번호");
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
