package com.shopwise.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.IOException;

/**
 * Filter to handle multipart form data requests properly
 * This ensures that multipart requests are properly processed before reaching the security filters
 */
@Component
public class MultipartRequestFilter extends OncePerRequestFilter {

    private final StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String contentType = request.getContentType();
        
        if (contentType != null && contentType.contains("multipart/form-data")) {
            // Log that we're processing a multipart request
            logger.debug("Processing multipart request: " + request.getRequestURI());
            
            // Only parse the request if it's not already a MultipartHttpServletRequest
            if (!(request instanceof MultipartHttpServletRequest) && multipartResolver.isMultipart(request)) {
                try {
                    // Parse the multipart request
                    request = multipartResolver.resolveMultipart(request);
                    logger.debug("Successfully parsed multipart request");
                } catch (Exception e) {
                    logger.error("Error parsing multipart request", e);
                }
            }
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
