package media_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Data;

@Document("media")
@Data
@Builder
public class Media {
    @Id
    private String id;
    @Field("image_path")
    private String imagePath;
    @Field("product_id")
    private String productId;
}
