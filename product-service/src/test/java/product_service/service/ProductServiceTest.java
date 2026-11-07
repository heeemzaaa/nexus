package product_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

import product_service.dto.UserDTO;
import product_service.dto.ProductDTO.ProductInput;
import product_service.dto.ProductDTO.ProductOutput;
import product_service.exception.BadRequestException;
import product_service.exception.NotFoundException;
import product_service.model.Product;
import product_service.model.ProductStatus;
import product_service.repository.ProductRepository;
import product_service.restApi.MediaClient;
import product_service.restApi.UserClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService unit tests")
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MediaClient mediaClient;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private ProductService productService;

    private ProductInput productInput;
    private Product productResponse;
    private UserDTO userResponse;

    @BeforeEach
    void setup() {
        productInput = new ProductInput("adidas", "good product", 8.5, 20);
        productResponse = Product.builder()
                .name(productInput.name())
                .description(productInput.description())
                .price(productInput.price())
                .quantity(productInput.quantity())
                .status(ProductStatus.PENDING)
                .userId("user-123")
                .id("a5SD45vsdf")
                .build();
        userResponse = UserDTO.builder()
                .name("Youssef")
                .avatar("https://profile.com/516")
                .build();
    }

    @DisplayName("Create product unit tests")
    @Nested
    class CreateProductTests {

        @Test
        void shouldCreateProductSuccessfully() {
            // Given
            final String userId = "1";
            when(productRepository.insert(any(Product.class)))
                    .thenReturn(productResponse);

            // When
            ProductOutput product = productService.createProduct(productInput, userId);

            // Then
            assertNotNull(product);
            assertEquals(productInput.name(), product.name());
            verify(productRepository).insert(any(Product.class));
        }
    }

    @DisplayName("Get products unit tests")
    @Nested
    class GetProductsTests {

        @Test
        void shouldGetProductByIdSuccessfully() {
            // Arrange
            final String productId = "product-id";
            final String userId = "user-123";
            when(productRepository.findById(productId))
                    .thenReturn(Optional.of(productResponse));
            when(mediaClient.getProductMedia(anyString()))
                    .thenReturn(List.of("media-url1", "media-url2"));

            // Act
            ProductOutput product = productService.getProduct(productId, userId);

            // Assert
            assertNotNull(product);
            assertEquals(productResponse.getName(), product.name());
            verify(productRepository).findById(productId);
            verify(mediaClient).getProductMedia(anyString());
        }

        @Test
        @DisplayName("Should get products successfully")
        void shouldGetProductsSuccessfully() {
            // Arrange
            final int page = 0;
            final int size = 10;
            when(productRepository
                    .findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                            any(), any(), any(), any(), any()))
                    .thenReturn(new PageImpl<Product>(List.of(productResponse)));
            when(userClient.getUserProducts(any()))
                    .thenReturn(Map.of("user-123", userResponse));
            when(mediaClient.getMediaProducts(any()))
                    .thenReturn(Map.of("media-123", List.of("url1", "url2"), "media-456",
                            List.of("url3", "url4")));

            // Act
            List<ProductOutput> products = productService.getProducts(page, size, null, "", "id");

            // Assert
            assertNotNull(products);
            assertEquals(1, products.size());
            verify(productRepository).findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                    any(), any(), any(), any(), any());
            verify(productRepository, times(0)).findByUserId(any(), any());
            verify(userClient).getUserProducts(any());
            verify(mediaClient).getMediaProducts(any());
        }

        @Test
        @DisplayName("Should get profile products successfully")
        void shouldGetPtofileProductsSuccessfully() {
            // Arrange
            final int page = 0;
            final int size = 10;
            final String userId = "user-123";
            when(productRepository.findByUserId(any(), any()))
                    .thenReturn(new PageImpl<Product>(List.of(productResponse)));
            when(userClient.getUserProducts(any()))
                    .thenReturn(Map.of("user-123", userResponse));
            when(mediaClient.getMediaProducts(any()))
                    .thenReturn(Map.of("media-123", List.of("url1", "url2"), "media-456",
                            List.of("url3", "url4")));

            // Act
            List<ProductOutput> products = productService.getProducts(page, size, userId, "",
                    "id");

            // Assert
            assertNotNull(products);
            assertEquals(1, products.size());
            verify(productRepository).findByUserId(any(), any());
            verify(productRepository, times(0)).findByStatus(any(), any());
            verify(userClient).getUserProducts(any());
            verify(mediaClient).getMediaProducts(any());
        }

        @Test
        @DisplayName("Should throw bad request exception when get products with invalid size")
        void shouldThrowBadRequestExceptionWhenGetProductsWithInvalidSize() {
            // Arrange
            final int page = 0;
            final int size = 101;
            final String userId = "user-id";

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.getProducts(page, size, userId, "", "id"));

            assertNotNull(exception);
            assertEquals("Max size is: 100", exception.getMessage());
        }

        @Test
        void shouldThrowNotFoundExceptionWhenProductNotFound() {
            // Given
            final String productId = "product-id";
            when(productRepository.findById(productId))
                    .thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> productService.getProduct(productId, anyString()));

            assertNotNull(exception);
            assertEquals("Whoops! product not found", exception.getMessage());
            verify(productRepository).findById(productId);
        }

    }

    @Nested
    @DisplayName("Update product unit tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            // Arrange
            final String productId = "product-123";
            final String userId = "user-123";
            when(productRepository.findById(productId))
                    .thenReturn(Optional.of(productResponse));
            when(productRepository.save(productResponse))
                    .thenReturn(productResponse);

            // Act
            ProductOutput product = productService.updateProduct(productId, productInput, userId);

            // Assert
            assertNotNull(product);
            assertEquals(productInput.name(), product.name());
            verify(productRepository).findById(productId);
            verify(productRepository).save(productResponse);
        }

        @Test
        @DisplayName("Should throw not found exception when updated product not found")
        void shouldThrowNotFoundExceptionWhenUpdatedProductNotFound() {
            // Arrange
            final String productId = "product-id";
            when(productRepository.findById(productId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> productService.updateProduct(productId, any(), "user-123"));

            assertNotNull(exception);
            assertEquals("Whoops! product not found", exception.getMessage());
            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("Should throw access denied exception when not the product owner")
        void ShouldThrowAccessDeniedExceptionWhenNotTheProductOwner() {
            // Arrange
            final String productId = "product-id";
            final String userId = "user-456";
            when(productRepository.findById(productId))
                    .thenReturn(Optional.of(productResponse));

            // Act & Assert
            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                    () -> productService.updateProduct(productId, productInput, userId));

            assertNotNull(exception);
            assertEquals("Cannot update products of others!", exception.getMessage());
        }

    }
}
