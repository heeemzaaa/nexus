package order_service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

public class ProductDTO {

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static record ProductOutput(
            String id,
            String name,
            String description,
            Double price,
            Integer quantity,
            // ProductStatus status,
            @JsonProperty("user_id") String userId,
            // @JsonProperty("user_infos") UserDTO userInfos,
            List<String> files) {
    }

}
