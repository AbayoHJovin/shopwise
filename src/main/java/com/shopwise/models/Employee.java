package com.shopwise.models;

import com.shopwise.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String email;
    private String password;
    private double salary;
    private LocalDate joinedDate;
    private boolean isDisabled;
    private boolean isCollaborator;
    private boolean emailConfirmed;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    private Business business;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<SalaryPayment> payments;
}