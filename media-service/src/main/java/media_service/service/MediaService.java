package media_service.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import media_service.exception.BadRequestException;
import media_service.kafka.MediaProducerService;
import media_service.model.Media;
import media_service.model.Target;
import media_service.model.dto.MediaDTO.MediaInput;
import media_service.model.dto.MediaDTO.MediaPathOutput;
import media_service.model.dto.ProductDTO.ProductInput;
import media_service.repository.MediaRepository;
import media_service.restApi.ProductClient;
import media_service.restApi.UserClient;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final CloudinaryService cloudinaryService;
    private final MediaProducerService mediaProducerService;

    private final String product_dir = "products";
    private final String user_dir = "avatars";

    public Map<String, Object> uploadMedia(MediaInput mediaInput, String userId) {

        if (mediaInput.files() == null || mediaInput.files().length == 0) {
            throw new BadRequestException("No files uploaded");
        }

        for (MultipartFile file : mediaInput.files()) {
            FileValidator.validateImage(file);
        }

        if (mediaInput.target() == Target.USER && mediaInput.files().length > 1) {
            throw new BadRequestException("Users accept only one media");
        } else if ((mediaInput.target().equals(Target.PRODUCT) || mediaInput.target().equals(Target.UPDATE_PRODUCT))
                && mediaInput.files().length > 5) {
            throw new BadRequestException("Products accept maximum 5 media");
        }

        if (mediaInput.target() == Target.USER && !mediaInput.targetId().equals(userId)) {
            throw new AccessDeniedException("Cannot update other users avatar");
        }

        if (mediaInput.target().equals(Target.PRODUCT) || mediaInput.target().equals(Target.UPDATE_PRODUCT)) {
            ProductInput product = productClient.getProduct(mediaInput.targetId());
            if (!product.user_id().equals(userId)) {
                throw new AccessDeniedException("Cannot update other users products");
            }

            if (mediaInput.target().equals(Target.PRODUCT)) {
                var mediaSize = this.mediaRepository.findByProductId(product.id()).size();
                if (mediaSize > 0) {
                    throw new BadRequestException("Cannot add another media product");
                }
            } else {
                this.mediaRepository.deleteByProductId(mediaInput.targetId());
            }
        }

        String subDir = (mediaInput.target().equals(Target.PRODUCT)
                || mediaInput.target().equals(Target.UPDATE_PRODUCT))
                        ? product_dir
                        : user_dir;
        Map<String, Object> response = new HashMap<>();
        List<String> message = new ArrayList<>();
        for (MultipartFile file : mediaInput.files()) {

            // String filePath = this.saveFile(mediaInput, location, file, subDir);
            String filePath = this.cloudinaryService.uploadFile(file, subDir);

            if (mediaInput.target() == Target.USER) {
                userClient.updateAvatar(filePath);
            }
            if (subDir.equals(product_dir)) {
                Media media = Media.builder()
                        .productId(mediaInput.targetId())
                        .imagePath(filePath)
                        .build();
                mediaRepository.save(media);
            }

            message.add(filePath);
        }

        if (subDir.equals(product_dir)) {
            this.mediaProducerService.sendMessage("update-product-status", mediaInput.targetId());
        }
        response.put("files", message);
        return response;
    }

    public List<String> getProductMedia(String productId) {
        List<MediaPathOutput> media = this.mediaRepository.findImagesPathByProductId(productId);
        return media.stream().map((ele) -> ele.imagePath()).toList();
    }

    public Map<String, List<String>> getMediaProducts(List<String> productIds) {
        List<Media> medias = mediaRepository.findByProductIdIn(productIds);

        return medias.stream()
                .collect(Collectors.groupingBy(Media::getProductId,
                        Collectors.mapping(Media::getImagePath, Collectors.toList())));
    }

}
