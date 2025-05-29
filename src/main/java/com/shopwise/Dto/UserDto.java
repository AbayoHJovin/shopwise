package com.shopwise.Dto;

import com.shopwise.Dto.subscription.SubscriptionInfoDto;
import com.shopwise.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private List<UUID> businessIds;
    private SubscriptionInfoDto subscription;

    /**
     * Convert User entity to UserDto
     * 
     * @param user The User entity to convert
     * @return UserDto representation of the user
     */
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .businessIds(user.getBusinesses() != null ? 
                        user.getBusinesses().stream()
                                .map(business -> business.getId())
                                .collect(Collectors.toList()) : 
                        null)
                .subscription(SubscriptionInfoDto.fromEntity(user.getSubscriptionInfo()))
                .build();
    }
}
