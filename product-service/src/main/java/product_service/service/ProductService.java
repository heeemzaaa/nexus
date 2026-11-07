package product_service.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import product_service.dto.ProductDTO.ProductInput;
import product_service.dto.ProductDTO.ProductOutput;
import product_service.dto.UserDTO;
import product_service.exception.BadRequestException;
import product_service.exception.NotFoundException;
import product_service.kafka.ProductProducerService;
import product_service.mapper.ProductMapper;
import product_service.model.Product;
import product_service.model.ProductStatus;
import product_service.repository.ProductRepository;
import product_service.restApi.MediaClient;
import product_service.restApi.UserClient;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MediaClient mediaClient;
    private final UserClient userClient;
    private final ProductProducerService productProducerService;

    public ProductOutput createProduct(ProductInput productData, String userId) {
        Product product = Product.builder()
                .name(productData.name())
                .description(productData.description())
                .price(productData.price())
                .quantity(productData.quantity())
                .userId(userId)
                .status(ProductStatus.PENDING)
                .build();

        Product createdProduct = this.productRepository.insert(product);
        return ProductMapper.toProductOutputDto(createdProduct, null, null);
    }

    public List<ProductOutput> getProducts(int page, int size, String userId, String search, String sortedBy) {
        if (size > 100) {
            throw new BadRequestException("Max size is: 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, sortedBy);

        List<Product> entities = userId == null
                ? this.productRepository
                        .findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                                ProductStatus.ACTIVE, search, ProductStatus.ACTIVE, search, pageable)
                        .getContent() // For home page
                // products
                : this.productRepository.findByUserId(userId, pageable).getContent(); // For profile page products

        return this.getUsersAndMediaOfProducts(entities);
    }

    public ProductOutput getProduct(final String productId, final String userId) {
        Product product = this.productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Whoops! product not found"));

        if (product.getStatus().equals(ProductStatus.PENDING) && !product.getUserId().equals(userId)) {
            throw new BadRequestException("Cannot get product with status pending");
        }

        List<String> productFiles = this.mediaClient.getProductMedia(product.getId());
        return ProductMapper.toProductOutputDto(product, productFiles, null);
    }

    public ProductOutput updateProduct(String productId, ProductInput productData, String userId) {
        Product product = this.productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Whoops! product not found"));

        if (!userId.equals(product.getUserId())) {
            throw new AccessDeniedException("Cannot update products of others!");
        }

        product.setName(productData.name());
        product.setDescription(productData.description());
        product.setPrice(productData.price());
        product.setQuantity(productData.quantity());

        this.productRepository.save(product);

        return ProductMapper.toProductOutputDto(product, null, null);
    }

    public Map<String, String> deleteProduct(String productId, String userId) {
        Product product = this.productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Whoops! product not found"));

        if (!userId.equals(product.getUserId())) {
            throw new AccessDeniedException("Cannot delete products of others!");
        }

        this.productRepository.delete(product);

        this.productProducerService.sendMessage("delete-media", product.getId());

        return Map.of(
                "productId", product.getId(),
                "message", "Product deleted successfully!");
    }

    public List<ProductOutput> getProductsCarts(final Set<String> productIds) {
        List<Product> entities = this.productRepository.findByIdIn(productIds);
        return this.getUsersAndMediaOfProducts(entities);
    }

    private List<ProductOutput> getUsersAndMediaOfProducts(final List<Product> entities) {
        // send one request to get users for each product
        Set<String> userIds = entities.stream().map(product -> product.getUserId()).collect(Collectors.toSet());
        Map<String, UserDTO> users = this.userClient.getUserProducts(userIds);

        // send one request to get media for each product
        List<String> productIds = entities.stream().map(product -> product.getId()).toList();
        Map<String, List<String>> mediaProducts = this.mediaClient.getMediaProducts(productIds);

        List<ProductOutput> products = entities.stream().map(product -> ProductMapper.toProductOutputDto(product,
                mediaProducts.get(product.getId()), users.get(product.getUserId()))).toList();

        return products;
    }

}
