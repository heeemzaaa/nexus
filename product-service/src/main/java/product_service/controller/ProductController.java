package product_service.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import product_service.dto.ProductDTO.ProductInput;
import product_service.dto.ProductDTO.ProductOutput;
import product_service.service.ProductService;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ProductOutput post(@RequestBody @Valid ProductInput productData, @AuthenticationPrincipal String userId) {
        return this.productService.createProduct(productData, userId);
    }

    @GetMapping
    public List<ProductOutput> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "id") String sortedBy) {

        return this.productService.getProducts(page, size, null, search, sortedBy);
    }

    @GetMapping("/users/{id}")
    public List<ProductOutput> getProfileProducts(@PathVariable("id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return this.productService.getProducts(page, size, userId, "", "id");
    }

    @GetMapping("/{id}")
    public ProductOutput get(@PathVariable("id") String productId, @AuthenticationPrincipal String userId) {
        return this.productService.getProduct(productId, userId);
    }

    @PutMapping("/{id}")
    public ProductOutput update(@PathVariable("id") String productId, @RequestBody @Valid ProductInput productData,
            @AuthenticationPrincipal String userId) {
        return this.productService.updateProduct(productId, productData, userId);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable("id") String productId, @AuthenticationPrincipal String userId) {
        return this.productService.deleteProduct(productId, userId);
    }

    @PostMapping("/carts")
    public List<ProductOutput> getProductsCarts(@RequestBody Set<String> productIds) {
        return this.productService.getProductsCarts(productIds);
    }

}
