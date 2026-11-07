package product_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Data;

@Document("product")
@Data
@Builder
public class Product {

    @Id
    private String id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private ProductStatus status;
    @Field("user_id")
    private String userId;

}
