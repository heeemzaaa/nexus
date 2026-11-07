package order_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import order_service.utils.Auditable;

@Document(collection = "cart_item")
@Builder
@Getter
@Setter
public class CartItem extends Auditable {
    @Id
    private String id;
    @Field(name = "shopping_cart_id")
    private String shoppingCartId;
    @Field(name = "product_id")
    private String productId;
    private Integer quantity;
}
