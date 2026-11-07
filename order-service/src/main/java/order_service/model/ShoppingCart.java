package order_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import order_service.utils.Auditable;

@Document(collection = "shopping_cart")
@Builder
@Getter
@Setter
public class ShoppingCart extends Auditable {
    @Id
    private String id;
    @Field(name = "user_id")
    private String userId;
}
