package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    
    @Query("SELECT b FROM Business b WHERE :user MEMBER OF b.collaborators")
    List<Business> findBusinessesByCollaborator(@Param("user") User user);
    
    /**
     * Check if a user is a collaborator of a business
     * 
     * @param businessId The business ID
     * @param userId The user ID
     * @return true if the user is a collaborator, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Business b JOIN b.collaborators c WHERE b.id = :businessId AND c.id = :userId")
    boolean isUserCollaborator(@Param("businessId") UUID businessId, @Param("userId") UUID userId);
    
    Optional<Business> findById(UUID id);
    
    /**
     * Find businesses by name
     * 
     * @param name The business name
     * @return List of businesses with the given name
     */
    List<Business> findByName(String name);
    
    /**
     * Find businesses by name containing the given string (case insensitive)
     * 
     * @param name The business name to search for
     * @param pageable Pagination information
     * @return Page of businesses with names containing the given string
     */
    Page<Business> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find businesses by administrative location
     * 
     * @param province The province name
     * @param pageable Pagination information
     * @return Page of businesses in the given province
     */
    Page<Business> findByLocationProvinceIgnoreCase(String province, Pageable pageable);
    
    /**
     * Find businesses by administrative location
     * 
     * @param district The district name
     * @param pageable Pagination information
     * @return Page of businesses in the given district
     */
    Page<Business> findByLocationDistrictIgnoreCase(String district, Pageable pageable);
    
    /**
     * Find businesses by administrative location
     * 
     * @param sector The sector name
     * @param pageable Pagination information
     * @return Page of businesses in the given sector
     */
    Page<Business> findByLocationSectorIgnoreCase(String sector, Pageable pageable);
    
    /**
     * Find businesses by administrative location
     * 
     * @param cell The cell name
     * @param pageable Pagination information
     * @return Page of businesses in the given cell
     */
    Page<Business> findByLocationCellIgnoreCase(String cell, Pageable pageable);
    
    /**
     * Find businesses by administrative location
     * 
     * @param village The village name
     * @param pageable Pagination information
     * @return Page of businesses in the given village
     */
    Page<Business> findByLocationVillageIgnoreCase(String village, Pageable pageable);
    
    /**
     * Find businesses by name and administrative location
     * 
     * @param name The business name to search for
     * @param province The province name
     * @param pageable Pagination information
     * @return Page of businesses matching both criteria
     */
    Page<Business> findByNameContainingIgnoreCaseAndLocationProvinceIgnoreCase(String name, String province, Pageable pageable);
    
    /**
     * Find businesses by name and administrative location
     * 
     * @param name The business name to search for
     * @param district The district name
     * @param pageable Pagination information
     * @return Page of businesses matching both criteria
     */
    Page<Business> findByNameContainingIgnoreCaseAndLocationDistrictIgnoreCase(String name, String district, Pageable pageable);
    
    /**
     * Find businesses by name and administrative location
     * 
     * @param name The business name to search for
     * @param sector The sector name
     * @param pageable Pagination information
     * @return Page of businesses matching both criteria
     */
    Page<Business> findByNameContainingIgnoreCaseAndLocationSectorIgnoreCase(String name, String sector, Pageable pageable);
    
    /**
     * Find businesses by name and administrative location
     * 
     * @param name The business name to search for
     * @param cell The cell name
     * @param pageable Pagination information
     * @return Page of businesses matching both criteria
     */
    Page<Business> findByNameContainingIgnoreCaseAndLocationCellIgnoreCase(String name, String cell, Pageable pageable);
    
    /**
     * Find businesses by name and administrative location
     * 
     * @param name The business name to search for
     * @param village The village name
     * @param pageable Pagination information
     * @return Page of businesses matching both criteria
     */
    Page<Business> findByNameContainingIgnoreCaseAndLocationVillageIgnoreCase(String name, String village, Pageable pageable);
    
    /**
     * Find all businesses that have both latitude and longitude coordinates
     * 
     * @param pageable Pagination information
     * @return Page of businesses with geolocation information
     */
    @Query("SELECT b FROM Business b WHERE b.location.latitude IS NOT NULL AND b.location.longitude IS NOT NULL")
    Page<Business> findAllWithCoordinates(Pageable pageable);
}
