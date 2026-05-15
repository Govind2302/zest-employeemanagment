package com.zest.test.employeemanagement.repository;

import com.zest.test.employeemanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Used to prevent duplicate email on create/update
    boolean existsByEmail(String email);
    Optional<Employee> findByEmail(String email);

    // Pagination + sorting by department
    Page<Employee> findByDepartmentId(Integer departmentId, Pageable pageable);

    // Pagination + sorting by active status
    Page<Employee> findByIsActive(Boolean isActive, Pageable pageable);

    // Search by name (partial match) with pagination
    @Query("SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Employee> searchByName(@Param("name") String name, Pageable pageable);

    // Full listing with JOIN FETCH to avoid N+1 problem during pagination
    @Query("SELECT e FROM Employee e JOIN FETCH e.department JOIN FETCH e.position")
    Page<Employee> findAllWithDetails(Pageable pageable);
}