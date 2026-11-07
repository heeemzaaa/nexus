package media_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import media_service.repository.MediaRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaConsumerService {
    private final MediaRepository mediaRepository;

    @KafkaListener(topics = "delete-media", groupId = "MediaGroup")
    public void deleteProductMedia(String productId) {
        try {
            this.mediaRepository.deleteByProductId(productId);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}