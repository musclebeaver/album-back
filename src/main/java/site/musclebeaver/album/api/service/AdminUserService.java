package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.musclebeaver.album.api.dto.UserUpdateRequestDto;
import site.musclebeaver.album.api.repository.AdminUserRepository;
import site.musclebeaver.album.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;

    // ✅ 페이징 전체 조회
    @Transactional(readOnly = true)
    public Page<UserEntity> getPagedUsers(Pageable pageable) {
        return adminUserRepository.findAll(pageable);
    }

    // ✅ 회원 정보 수정
    @Transactional
    public void updateUser(Long userId, UserUpdateRequestDto request) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        if (request.getIsApproved() != null) {
            user.setIsApproved(request.getIsApproved());
        }

        if (request.getIsAdmin() != null) {
            user.setIsAdmin(request.getIsAdmin());
        }
    }

    // ✅ 회원 삭제
    @Transactional
    public void deleteUser(Long userId) {
        adminUserRepository.deleteById(userId);
    }

    // ✅ 비밀번호 초기화
    @Transactional
    public void resetPassword(Long userId) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        user.setPassword("initialPassword");  // ⚠️ 운영 시 반드시 암호화
    }

    // ✅ 로그인 실패 횟수 초기화
    @Transactional
    public void resetFailedLoginCount(Long userId) {
        UserEntity user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        user.setFailedLoginCount(0);
    }
        // ✅ 검색 기능 추가
    @Transactional(readOnly = true)
    public List<UserEntity> searchUsers(String keyword) {
        return adminUserRepository.findByUsernameContainingOrEmailContaining(keyword, keyword);
    }
}
