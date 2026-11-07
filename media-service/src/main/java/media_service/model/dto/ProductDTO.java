package media_service.model.dto;

public class ProductDTO {

    public static record ProductInput(
            String id,
            String name,
            String discription,
            Double price,
            Integer quantity,
            String user_id) {
    }

}
