package com.shopwise.Repository;

import com.shopwise.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Employee findByEmail(String email);

    List<Employee> findByBusiness_Id(UUID businessId);
}
