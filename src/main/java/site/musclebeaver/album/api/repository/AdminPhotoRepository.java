package site.musclebeaver.album.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import site.musclebeaver.album.api.dto.PhotoAdminResponseDto;

public interface AdminPhotoRepository extends Repository<site.musclebeaver.album.api.entity.Photo, Long> {

    @Query("""
        SELECT new site.musclebeaver.album.api.dto.PhotoAdminResponseDto(
            p.id, p.title, p.description, p.imageUrl, p.createdAt,
            f.id, f.name,
            u.id, u.username
        )
        FROM Photo p
        JOIN p.folder f
        JOIN f.user u
        """)
    Page<PhotoAdminResponseDto> findAllDtos(Pageable pageable);

    @Query("""
        SELECT new site.musclebeaver.album.api.dto.PhotoAdminResponseDto(
            p.id, p.title, p.description, p.imageUrl, p.createdAt,
            f.id, f.name,
            u.id, u.username
        )
        FROM Photo p
        JOIN p.folder f
        JOIN f.user u
        WHERE u.username LIKE %:keyword%
        """)
    Page<PhotoAdminResponseDto> findByUsernameContainingDtos(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT new site.musclebeaver.album.api.dto.PhotoAdminResponseDto(
            p.id, p.title, p.description, p.imageUrl, p.createdAt,
            f.id, f.name,
            u.id, u.username
        )
        FROM Photo p
        JOIN p.folder f
        JOIN f.user u
        WHERE f.name LIKE %:keyword%
        """)
    Page<PhotoAdminResponseDto> findByFolderNameContainingDtos(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT new site.musclebeaver.album.api.dto.PhotoAdminResponseDto(
            p.id, p.title, p.description, p.imageUrl, p.createdAt,
            f.id, f.name,
            u.id, u.username
        )
        FROM Photo p
        JOIN p.folder f
        JOIN f.user u
        WHERE p.title LIKE %:keyword%
        """)
    Page<PhotoAdminResponseDto> findByPhotoTitleContainingDtos(@Param("keyword") String keyword, Pageable pageable);

    void deleteById(Long id);
}
