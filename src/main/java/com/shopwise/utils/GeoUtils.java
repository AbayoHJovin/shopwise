package com.shopwise.utils;

/**
 * Utility class for geospatial calculations
 */
public class GeoUtils {
    
    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers
    
    /**
     * Calculate the distance between two points using the Haversine formula
     * 
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        // Haversine formula
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Format distance for display
     * 
     * @param distanceKm Distance in kilometers
     * @return Formatted distance string
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            // Convert to meters for distances less than 1 km
            int meters = (int) (distanceKm * 1000);
            return meters + " m";
        } else if (distanceKm < 10.0) {
            // Show one decimal place for distances less than 10 km
            return String.format("%.1f km", distanceKm);
        } else {
            // Round to nearest integer for larger distances
            return Math.round(distanceKm) + " km";
        }
    }
}
