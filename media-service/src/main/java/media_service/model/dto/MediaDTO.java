package media_service.model.dto;


import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import media_service.model.Target;

public class MediaDTO {

    public static record MediaInput(
            @NotNull(message = "Target is required")
            Target target,
            @NotNull(message = "Target id is required")
            @JsonProperty("target_id") String targetId,
            @NotNull(message = "Files are required")
            MultipartFile[] files) {
    }

    public static record MediaPathOutput(
        @JsonProperty("image_path")
        String imagePath
    ) {
    }

}
