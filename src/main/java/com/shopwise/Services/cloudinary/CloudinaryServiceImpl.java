package com.shopwise.Services.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public Map<String, String> uploadImage(MultipartFile file, String folder) {
        try {
            // Validate file
            validateFile(file);
            
            // Create a unique public ID with folder path
            String publicId = folder + "/" + UUID.randomUUID().toString();
            
            // Upload options
            Map<String, Object> options = new HashMap<>();
            options.put("public_id", publicId);
            options.put("overwrite", true);
            options.put("resource_type", "image");
            
            // Upload to Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            
            // Extract and return relevant information
            Map<String, String> result = new HashMap<>();
            result.put("publicId", uploadResult.get("public_id").toString());
            result.put("imageUrl", uploadResult.get("secure_url").toString());
            
            log.info("Image uploaded successfully to Cloudinary: {}", result.get("publicId"));
            return result;
            
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            throw CloudinaryException.internalServerError("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteImage(String publicId) {
        try {
            // Delete from Cloudinary
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            // Check if deletion was successful
            String status = result.get("result").toString();
            boolean success = "ok".equals(status);
            
            if (success) {
                log.info("Image deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn("Failed to delete image from Cloudinary: {}, Status: {}", publicId, status);
            }
            
            return success;
            
        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary", e);
            throw CloudinaryException.internalServerError("Failed to delete image: " + e.getMessage());
        }
    }
    
    /**
     * Validates that the file is a valid image
     * 
     * @param file The file to validate
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw CloudinaryException.badRequest("File is empty");
        }
        
        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw CloudinaryException.badRequest("File size exceeds maximum limit of 10MB");
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw CloudinaryException.badRequest("File must be an image");
        }
        
        // Check supported image formats
        if (!(contentType.equals("image/jpeg") || 
              contentType.equals("image/png") || 
              contentType.equals("image/gif") || 
              contentType.equals("image/webp"))) {
            throw CloudinaryException.badRequest("Unsupported image format. Supported formats: JPEG, PNG, GIF, WEBP");
        }
    }
}
