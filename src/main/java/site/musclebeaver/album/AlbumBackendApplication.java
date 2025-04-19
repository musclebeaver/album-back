package site.musclebeaver.album; // 패키지 위치 변경

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "site.musclebeaver.album")
@EnableJpaAuditing
public class AlbumBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlbumBackendApplication.class, args);
    }
}