package com.zest.test.employeemanagement.service;

import com.zest.test.employeemanagement.dto.EmployeeDTO;
import com.zest.test.employeemanagement.entity.Department;
import com.zest.test.employeemanagement.entity.Employee;
import com.zest.test.employeemanagement.entity.Position;
import com.zest.test.employeemanagement.exception.ResourceNotFoundException;
import com.zest.test.employeemanagement.repository.DepartmentRepository;
import com.zest.test.employeemanagement.repository.EmployeeRepository;
import com.zest.test.employeemanagement.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    // ----------------------------------------------------------------
    // GET all employees — paginated + sorted
    // ----------------------------------------------------------------
    public Page<EmployeeDTO> getAllEmployees(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findAll(pageable).map(this::toDTO);
    }

    // ----------------------------------------------------------------
    // GET employee by ID
    // ----------------------------------------------------------------
    public EmployeeDTO getEmployeeById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return toDTO(emp);
    }

    // ----------------------------------------------------------------
    // GET employees by department — paginated
    // ----------------------------------------------------------------
    public Page<EmployeeDTO> getEmployeesByDepartment(Integer deptId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.findByDepartmentId(deptId, pageable).map(this::toDTO);
    }

    // ----------------------------------------------------------------
    // GET employees by name search — paginated
    // ----------------------------------------------------------------
    public Page<EmployeeDTO> searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRepository.searchByName(name, pageable).map(this::toDTO);
    }

    // ----------------------------------------------------------------
    // POST — create new employee
    // ----------------------------------------------------------------
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        // Reject creation if email is already registered
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already in use.");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));
        Position position = positionRepository.findById(dto.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with id: " + dto.getPositionId()));

        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setSalary(dto.getSalary());
        employee.setDateOfJoining(dto.getDateOfJoining());
        employee.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        return toDTO(employeeRepository.save(employee));
    }

    // ----------------------------------------------------------------
    // PUT — update existing employee (full update)
    // ----------------------------------------------------------------
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Reject update if the new email is already used by a DIFFERENT employee
        Optional<Employee> existingWithEmail = employeeRepository.findByEmail(dto.getEmail());
        if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already in use by another employee.");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));
        Position position = positionRepository.findById(dto.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with id: " + dto.getPositionId()));

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setSalary(dto.getSalary());
        employee.setDateOfJoining(dto.getDateOfJoining());
        if (dto.getIsActive() != null) {
            employee.setIsActive(dto.getIsActive());
        }

        return toDTO(employeeRepository.save(employee));
    }

    // ----------------------------------------------------------------
    // DELETE — soft delete (sets is_active = false)
    // ----------------------------------------------------------------
    @Transactional
    public String deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setIsActive(false);
        employeeRepository.save(employee);
        return "Employee with id " + id + " has been deactivated.";
    }

    // ----------------------------------------------------------------
    // Entity → DTO mapper
    // ----------------------------------------------------------------
    private EmployeeDTO toDTO(Employee emp) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(emp.getId());
        dto.setName(emp.getName());
        dto.setEmail(emp.getEmail());
        dto.setDepartmentId(emp.getDepartment() != null ? emp.getDepartment().getId() : null);
        dto.setDepartmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null);
        dto.setPositionId(emp.getPosition() != null ? emp.getPosition().getId() : null);
        dto.setPositionName(emp.getPosition() != null ? emp.getPosition().getTitle() : null);
        dto.setSalary(emp.getSalary());
        dto.setDateOfJoining(emp.getDateOfJoining());
        dto.setIsActive(emp.getIsActive());
        return dto;
    }
}
