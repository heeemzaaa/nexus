package order_service.mapper;

import order_service.dto.CartDTO.CartItemOutput;
import order_service.dto.ProductDTO.ProductOutput;
import order_service.model.CartItem;

public class CartItemMapper {

    public static CartItemOutput toCartItemOutput(final CartItem cartItem, final ProductOutput product) {
        return CartItemOutput.builder()
                .id(cartItem.getId())
                .shoppingCartId(cartItem.getShoppingCartId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .product(product)
                .build();
    }

}
