package order_service.dto;

import java.time.Instant;
import lombok.Builder;

public class OrderDTO {
    
    @Builder
    public static record SoldProductOutput(
        String orderId,
        String orderStatus,
        String productId,
        String productName,
        Integer quantity,
        Double price,
        Instant createdAt
    ) {}
}
