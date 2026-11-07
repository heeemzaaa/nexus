package order_service.config;

import feign.codec.ErrorDecoder;
import order_service.restApi.CustomFeignErrorDecoder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfiguration {

    @Bean
    public ErrorDecoder customFeignErrorDecoder() {
        return new CustomFeignErrorDecoder();
    }
}