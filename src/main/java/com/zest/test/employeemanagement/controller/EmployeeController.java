package com.zest.test.employeemanagement.controller;

import com.zest.test.employeemanagement.dto.EmployeeDTO;
import com.zest.test.employeemanagement.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ----------------------------------------------------------------
    // GET /api/employees
    // Params: page, size, sortBy, direction
    // Example: /api/employees?page=0&size=5&sortBy=name&direction=asc
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @RequestParam(defaultValue = "0")           int page,
            @RequestParam(defaultValue = "10")          int size,
            @RequestParam(defaultValue = "name")        String sortBy,
            @RequestParam(defaultValue = "asc")         String direction) {

        Page<EmployeeDTO> result = employeeService.getAllEmployees(
                page, size, sortBy, direction);
        return ResponseEntity.ok(result);
    }

    // ----------------------------------------------------------------
    // GET /api/employees/{id}
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // ----------------------------------------------------------------
    // GET /api/employees/department/{deptId}
    // Params: page, size
    // Example: /api/employees/department/1?page=0&size=5
    // ----------------------------------------------------------------
    @GetMapping("/department/{deptId}")
    public ResponseEntity<Page<EmployeeDTO>> getByDepartment(
            @PathVariable Integer deptId,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size) {

        Page<EmployeeDTO> result = employeeService
                .getEmployeesByDepartment(deptId, page, size);
        return ResponseEntity.ok(result);
    }

    // ----------------------------------------------------------------
    // GET /api/employees/search?name=rohit
    // Params: name, page, size
    // ----------------------------------------------------------------
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeDTO>> searchByName(
            @RequestParam                           String name,
            @RequestParam(defaultValue = "0")       int page,
            @RequestParam(defaultValue = "10")      int size) {

        Page<EmployeeDTO> result = employeeService.searchByName(name, page, size);
        return ResponseEntity.ok(result);
    }

    // ----------------------------------------------------------------
    // POST /api/employees
    // Body: EmployeeDTO (id field ignored on create)
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(
            @RequestBody EmployeeDTO dto) {

        EmployeeDTO created = employeeService.createEmployee(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // ----------------------------------------------------------------
    // PUT /api/employees/{id}
    // Body: EmployeeDTO (full update)
    // ----------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeDTO dto) {

        EmployeeDTO updated = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(updated);
    }

    // ----------------------------------------------------------------
    // DELETE /api/employees/{id}
    // Soft delete — sets is_active = false
    // ----------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEmployee(
            @PathVariable Long id) {

        String message = employeeService.deleteEmployee(id);
        return ResponseEntity.ok(Map.of("message", message));
    }
}