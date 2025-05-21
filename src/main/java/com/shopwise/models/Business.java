package com.shopwise.models;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String location;
    private String about;
    private String websiteLink;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<Product> products;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<Employee> employees;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<Expense> expenses;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<SaleRecord> saleRecords;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<DailySummary> summaries;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<CollaborationRequest> collaborationRequests;

    @ManyToMany
    private List<User> collaborators;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<AvailabilitySlot> availability;
}
