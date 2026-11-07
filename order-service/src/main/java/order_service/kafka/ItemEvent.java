package order_service.kafka;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemEvent {

    private Boolean isIncrement;
    private Map<String, Integer> items;

}