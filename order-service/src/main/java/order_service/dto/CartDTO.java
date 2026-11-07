package order_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import order_service.dto.ProductDTO.ProductOutput;

public class CartDTO {

    public static record CartItemInput(
            @JsonProperty("product_id") @NotNull(message = "Product id cannot be empty  ") String productId,
            @NotNull(message = "Product quantity cannot be empty") @Positive(message = "Quantity must be 1 or more") Integer quantity) {
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static record CartItemOutput(
            String id,
            @JsonProperty("shopping_cart_id") String shoppingCartId,
            @JsonProperty("product_id") String productId,
            @JsonProperty("item_quantity") Integer quantity,
            ProductOutput product) {
    }

}
