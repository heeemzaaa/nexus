package order_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "order_item")
@Builder
@Getter
@Setter
public class OrderItem {
    @Id
    private String id;
    @Field(name = "order_id")
    private String orderId;
    @Field(name = "product_id")
    private String productId;
    @Field(name = "quantity")
    private Integer quantity;
    @Field(name = "checkout_price")
    private Double checkoutPrice;
}
