package com.shopwise.Services.cloudinary;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
    
    /**
     * Upload an image to Cloudinary
     * 
     * @param file The image file to upload
     * @param folder The folder to upload to (e.g., "products")
     * @return Map containing the upload result with public_id and secure_url
     */
    Map<String, String> uploadImage(MultipartFile file, String folder);
    
    /**
     * Delete an image from Cloudinary
     * 
     * @param publicId The public ID of the image to delete
     * @return true if deletion was successful
     */
    boolean deleteImage(String publicId);
}
