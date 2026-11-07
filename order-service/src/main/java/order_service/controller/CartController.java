package order_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import order_service.dto.CartDTO.CartItemInput;
import order_service.dto.CartDTO.CartItemOutput;
import order_service.service.CartService;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public CartItemOutput post(@Valid @RequestBody CartItemInput cartItem,
            @AuthenticationPrincipal String userId) {
        return this.cartService.createCartItem(cartItem, userId);
    }

    @GetMapping
    public List<CartItemOutput> getItems(@AuthenticationPrincipal String userId) {
        return this.cartService.getItems(userId);
    }

    @PutMapping("/{id}")
    public CartItemOutput update(@PathVariable("id") String itemId, @Valid @RequestBody CartItemInput cartItemData,
            @AuthenticationPrincipal String userId) {
        return this.cartService.updateItem(itemId, cartItemData, userId);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable("id") String itemId, @AuthenticationPrincipal String userId) {
        return this.cartService.deleteItem(itemId, userId);
    }

}
