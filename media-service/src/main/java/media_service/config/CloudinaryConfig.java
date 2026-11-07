package media_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {
    @Value("${CLOUDINARY_URL:default}")
    private String cloudUrl;

    @Bean
    public Cloudinary cloudinary(){
        return new Cloudinary(cloudUrl);
    }
}