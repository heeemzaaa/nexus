package media_service.config;

import feign.codec.ErrorDecoder;
import media_service.restApi.CustomFeignErrorDecoder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfiguration {

    @Bean
    public ErrorDecoder customFeignErrorDecoder() {
        return new CustomFeignErrorDecoder();
    }
}