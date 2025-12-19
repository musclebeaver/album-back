package site.musclebeaver.album.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ S3 도입으로 더 이상 로컬 경로가 필요 없으므로 주석 처리
    // @Value("${album.upload-dir}")
    // private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ 로컬 파일 서빙 기능 비활성화
        // registry
        //         .addResourceHandler("/img/**")
        //         .addResourceLocations("file:///" + uploadDir);
    }
}