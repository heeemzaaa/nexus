package product_service.mapper;

import java.util.List;

import product_service.dto.ProductDTO.ProductOutput;
import product_service.dto.UserDTO;
import product_service.model.Product;

public class ProductMapper {

    public static ProductOutput toProductOutputDto(Product product, List<String> files, UserDTO user) {
        return ProductOutput.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .status(product.getStatus())
                .userId(product.getUserId())
                .userInfos(user)
                .files(files)
                .build();
    }

}
