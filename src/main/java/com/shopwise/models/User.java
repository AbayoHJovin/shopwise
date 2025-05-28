package com.shopwise.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String phone;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRoles role = UserRoles.USER;
    @ManyToMany(mappedBy = "collaborators")
    private List<Business> businesses;
    
    @Embedded
    private SubscriptionInfo subscriptionInfo = new SubscriptionInfo();

    public User(UUID id, String name, String phone, String email, String password) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.subscriptionInfo = new SubscriptionInfo();
    }

    public enum UserRoles{
        USER,
        ADMIN
    }
}
