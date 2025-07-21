package site.musclebeaver.album.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import site.musclebeaver.album.api.dto.PhotoAdminResponseDto;

public interface AdminPhotoRepository extends Repository<site.musclebeaver.album.api.entity.Photo, Long> {

    @Query("""
        SELECT new site.musclebeaver.album.api.dto.PhotoAdminResponseDto(p)
        FROM Photo p
        JOIN p.folder f
        JOIN f.user u
        """)
    Page<PhotoAdminResponseDto> findAllDtos(Pageable pageable);

    @Query("""
        SELECT new site.musclebeaver.album.api.dto.PhotoAdminResponseDto(p)
        FROM Photo p
        JOIN p.folder f
        JOIN f.user u
        WHERE u.username LIKE %:username%
        """)
    Page<PhotoAdminResponseDto> findByUsernameContainingDtos(@Param("username") String username, Pageable pageable);

    @Query("""
        SELECT new site.musclebeaver.album.api.dto.PhotoAdminResponseDto(p)
        FROM Photo p
        JOIN p.folder f
        JOIN f.user u
        WHERE f.name LIKE %:folderName%
        """)
    Page<PhotoAdminResponseDto> findByFolderNameContainingDtos(@Param("folderName") String folderName, Pageable pageable);

    @Query("""
        SELECT new site.musclebeaver.album.api.dto.PhotoAdminResponseDto(p)
        FROM Photo p
        JOIN p.folder f
        JOIN f.user u
        WHERE p.title LIKE %:title%
        """)
    Page<PhotoAdminResponseDto> findByPhotoTitleContainingDtos(@Param("title") String title, Pageable pageable);

    void deleteById(Long id);
}
