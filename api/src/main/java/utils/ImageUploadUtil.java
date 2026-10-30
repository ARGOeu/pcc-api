package utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

@ApplicationScoped
public class ImageUploadUtil {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public String saveBase64Image(String baseDir, String base64, String filenameWithoutExt,String subPath) {

        try {
            if (base64 == null || base64.isBlank()) {
                return null;
            }

            // Validate data URI
            if (!base64.startsWith("data:")) {
                throw new BadRequestException("Missing image MIME type prefix (data URI)");
            }

            int semiColonIndex = base64.indexOf(';');
            if (semiColonIndex < 0) {
                throw new BadRequestException("Invalid data URI format");
            }

            var mimeType = base64.substring(5, semiColonIndex);
            var ext = switch (mimeType) {
                case "image/png" -> ".png";
                case "image/jpeg", "image/jpg" -> ".jpg";
                default -> throw new BadRequestException("Only PNG and JPEG images are supported.");
            };

            // Extract pure base64 data
            var imageData = base64.substring(base64.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            if (imageBytes.length > MAX_FILE_SIZE) {
                throw new BadRequestException("Image size exceeds 5MB limit");
            }

            // Prepare folder and file
            var uploadDir = new File(baseDir);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new InternalServerErrorException("Failed to create upload directory");
            }

            var imageFile = new File(uploadDir, filenameWithoutExt + ext);
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(imageBytes);
            }

            // Return relative path (for DB storage or response)
            return subPath + filenameWithoutExt + ext;

        } catch (BadRequestException | InternalServerErrorException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid Base64 encoding", e);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to save image", e);
        }
    }


    public void validateBase64Image(String base64Image) {
        if (base64Image == null || base64Image.isBlank()) {
            throw new IllegalArgumentException("Logo image cannot be empty.");
        }

        if (!base64Image.startsWith("data:image/")) {
            throw new IllegalArgumentException("Logo must start with data:image/ MIME prefix.");
        }

        if (!base64Image.contains("base64,")) {
            throw new IllegalArgumentException("Logo must contain valid Base64 data.");
        }

        // Extract actual base64 part
        var base64Part = base64Image.substring(base64Image.indexOf("base64,") + 7).trim();

        try {
            Base64.getDecoder().decode(base64Part);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encoding for logo image.");
        }

        if (base64Part.length() < 50) { // prevent 1-pixel junk
            throw new IllegalArgumentException("Logo image appears too small or invalid.");
        }
    }

    public void deleteImageIfExists(String baseDir, String filenameWithoutExt) {
        var dir = new File(baseDir);
        if (!dir.exists()) return;

        var png = new File(dir, filenameWithoutExt + ".png");
        var jpg = new File(dir, filenameWithoutExt + ".jpg");

        if (png.exists()) png.delete();
        if (jpg.exists()) jpg.delete();
    }
}
