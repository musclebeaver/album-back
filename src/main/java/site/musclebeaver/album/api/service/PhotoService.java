package site.musclebeaver.album.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.musclebeaver.album.api.entity.Folder;
import site.musclebeaver.album.api.entity.Photo;
import site.musclebeaver.album.api.repository.FolderRepository;
import site.musclebeaver.album.api.repository.PhotoRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final FolderRepository folderRepository;

    // 페도라 서버에 저장할 경로
    private final String UPLOAD_DIR = "/img/uploads/";

    //  모든 사진 조회
    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }

    //  특정 폴더의 사진 조회
    public List<Photo> getPhotosByFolderId(Long folderId) {
        return photoRepository.findByFolder_Id(folderId);
    }

    //  단일 사진 조회
    public Photo getPhotoById(Long id) {
        return photoRepository.findById(id).orElse(null);
    }

    //  사진 업로드 및 저장 (페도라 서버에 저장)
    public Photo savePhoto(String title, String description, Long folderId, MultipartFile file) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        //  디렉토리 존재 여부 확인 및 생성
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        //  고유한 파일명 생성
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = UPLOAD_DIR + fileName;

        //  파일 저장
        file.transferTo(new File(filePath));

        //  저장된 파일의 접근 가능한 URL 생성
        String imageUrl = "/img/uploads/" + fileName;

        //  Photo 엔티티 저장
        Photo photo = new Photo();
        photo.setTitle(title);
        photo.setDescription(description);
        photo.setImageUrl(imageUrl);
        photo.setFolder(folder);

        return photoRepository.save(photo);
    }
     //  대량 사진 업로드 (추가된 로직)
    public List<Photo> saveMultiplePhotos(Long folderId, List<MultipartFile> files) throws IOException {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        List<Photo> savedPhotos = new ArrayList<>();

        //  저장 경로 확인 및 생성
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue; // 빈 파일 건너뛰기

            //  파일 저장
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = UPLOAD_DIR + fileName;
            file.transferTo(new File(filePath));

            //  저장된 파일 URL 설정
            String imageUrl = "/img/uploads/" + fileName;

            //  Photo 엔티티 생성 및 저장
            Photo photo = new Photo();
            photo.setTitle(file.getOriginalFilename()); // 기본적으로 파일명을 제목으로 설정
            photo.setDescription("Uploaded via bulk upload");
            photo.setImageUrl(imageUrl);
            photo.setFolder(folder);

            savedPhotos.add(photoRepository.save(photo));
        }

        return savedPhotos;
    }

    //  사진 삭제
    public void deletePhoto(Long id) {
        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isPresent()) {
            Photo photo = photoOpt.get();

            //  파일 삭제 (서버에서 제거)
            File file = new File(UPLOAD_DIR + photo.getImageUrl().replace("/img/uploads/", ""));
            if (file.exists()) {
                file.delete();
            }

            //  데이터베이스에서 사진 정보 삭제
            photoRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Photo not found with id: " + id);
        }
    }
}
