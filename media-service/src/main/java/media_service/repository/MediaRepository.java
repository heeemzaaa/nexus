package media_service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import media_service.model.Media;
import media_service.model.dto.MediaDTO.MediaPathOutput;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {

    List<Media> findByProductId(String productId);

    @Query(value = "{ 'product_id': ?0 }", fields = "{'image_path': 1, '_id': 0}")
    List<MediaPathOutput> findImagesPathByProductId(String productId);

    List<Media> findByProductIdIn(List<String> productIds);

    void deleteByProductId(String productId);
}
