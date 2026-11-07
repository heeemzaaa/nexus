package media_service.service;

import java.io.IOException;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import media_service.exception.BadRequestException;

public class FileValidator {
    private static final Tika TIKA = new Tika();

    public static void validateImage(MultipartFile file) {
        try {
            // Detect based on actual file content (magic bytes)
            String detectedType = TIKA.detect(file.getInputStream());
            
            if ( detectedType.startsWith("image/svg") || !detectedType.startsWith("image/") ) {
                throw new BadRequestException("Invalid file type. Only images are allowed.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read file for validation", e);
        }
    }

    public static String getExtensionFromMimeType(MultipartFile file) {
        try {
            String detectedType = TIKA.detect(file.getInputStream()); // e.g., "image/jpeg"
            
            return switch (detectedType) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/gif" -> "gif";
                case "image/webp" -> "webp";
                default -> {
                    // Fallback: extract substring after "image/" if it's a standard format
                    if (detectedType.startsWith("image/")) {
                        yield detectedType.substring(6); 
                    }
                    throw new BadRequestException("Unsupported image format: " + detectedType);
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Could not read file to extract extension", e);
        }
    }
}