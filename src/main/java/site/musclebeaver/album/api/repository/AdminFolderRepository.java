package site.musclebeaver.album.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.dto.FolderAdminResponseDto;

public interface AdminFolderRepository extends JpaRepository<Folder, Long> {

    @Query("""
        select new site.musclebeaver.album.api.dto.FolderAdminResponseDto(
            f.id,
            f.name,
            f.user.id,
            f.user.username,
            count(p)
        )
        from Folder f
        left join f.photos p
        group by f.id, f.name, f.user.id, f.user.username
    """)
    Page<FolderAdminResponseDto> findAllDtos(Pageable pageable);

    @Query("""
        select new site.musclebeaver.album.api.dto.FolderAdminResponseDto(
            f.id,
            f.name,
            f.user.id,
            f.user.username,
            count(p)
        )
        from Folder f
        left join f.photos p
        where f.user.username like %:username%
        group by f.id, f.name, f.user.id, f.user.username
    """)
    Page<FolderAdminResponseDto> findByUsernameContainingDtos(@Param("username") String username, Pageable pageable);

    @Query("""
        select new site.musclebeaver.album.api.dto.FolderAdminResponseDto(
            f.id,
            f.name,
            f.user.id,
            f.user.username,
            count(p)
        )
        from Folder f
        left join f.photos p
        where f.name like %:folderName%
        group by f.id, f.name, f.user.id, f.user.username
    """)
    Page<FolderAdminResponseDto> findByFolderNameContainingDtos(@Param("folderName") String folderName, Pageable pageable);
}
